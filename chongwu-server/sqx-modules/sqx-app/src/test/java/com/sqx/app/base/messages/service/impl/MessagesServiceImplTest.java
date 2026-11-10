package com.sqx.app.base.messages.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.messages.db.service.IMessagesService;
import com.sqx.app.base.messages.domain.bo.MessagesBroadcastBo;
import com.sqx.app.base.messages.domain.bo.MessagesInfoBo;
import com.sqx.app.base.messages.domain.bo.MessagesPageBo;
import com.sqx.app.base.messages.domain.bo.MessagesSaveBo;
import com.sqx.app.base.messages.domain.bo.MessagesUpdateBo;
import com.sqx.app.base.messages.domain.entity.Messages;
import com.sqx.app.base.messages.domain.vo.MessagesVo;
import com.sqx.common.core.exception.BizException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessagesServiceImplTest {

    @Mock
    private IMessagesService iMessagesService;

    @InjectMocks
    private MessagesServiceImpl messagesService;

    @Test
    void saveInfoPersistsEntity() {
        MessagesSaveBo bo = new MessagesSaveBo();
        bo.setTitle("t");
        bo.setContent("c");
        bo.setUserId(1L);
        messagesService.saveInfo(bo);
        verify(iMessagesService).save(any(Messages.class));
    }

    @Test
    void pageInfoDelegatesToDbService() {
        MessagesPageBo bo = new MessagesPageBo();
        bo.setPage(1L);
        bo.setLimit(10L);
        IPage<MessagesVo> page = new Page<>();
        when(iMessagesService.pageInfo(any(Page.class), any(MessagesPageBo.class))).thenReturn(page);
        assertEquals(page, messagesService.pageInfo(bo));
    }

    @Test
    void infoThrowsWhenMissing() {
        MessagesInfoBo bo = new MessagesInfoBo();
        bo.setId(99L);
        when(iMessagesService.getById(99L)).thenReturn(null);
        assertThrows(BizException.class, () -> messagesService.info(bo));
    }

    @Test
    void infoReturnsVoWhenExists() {
        MessagesInfoBo bo = new MessagesInfoBo();
        bo.setId(1L);
        Messages entity = new Messages();
        entity.setId(1L);
        entity.setTitle("hello");
        when(iMessagesService.getById(1L)).thenReturn(entity);
        MessagesVo vo = messagesService.info(bo);
        assertEquals("hello", vo.getTitle());
    }

    @Test
    void updateAndDelete() {
        MessagesInfoBo infoBo = new MessagesInfoBo();
        infoBo.setId(2L);
        MessagesUpdateBo updateBo = new MessagesUpdateBo();
        updateBo.setId(2L);
        messagesService.updateInfo(updateBo);
        messagesService.delete(infoBo);
        verify(iMessagesService).updateById(any(Messages.class));
        verify(iMessagesService).removeById(2L);
    }

    @Test
    void unreadCountReturnsZeroForNullUser() {
        assertEquals(0L, messagesService.unreadCount(null));
        verify(iMessagesService, never()).lambdaQuery();
    }

    @Test
    void markReadValidatesOwnershipAndIdempotency() {
        Messages owned = new Messages();
        owned.setId(1L);
        owned.setUserId(10L);
        owned.setIsRead(0);
        when(iMessagesService.getById(1L)).thenReturn(owned);
        messagesService.markRead(10L, 1L);
        verify(iMessagesService).updateById(any(Messages.class));

        owned.setIsRead(1);
        messagesService.markRead(10L, 1L);
        verify(iMessagesService, times(1)).updateById(any(Messages.class));
    }

    @Test
    void markReadRejectsForeignMessage() {
        Messages owned = new Messages();
        owned.setId(1L);
        owned.setUserId(10L);
        when(iMessagesService.getById(1L)).thenReturn(owned);
        assertThrows(BizException.class, () -> messagesService.markRead(11L, 1L));
    }

    @Test
    void markAllReadReturnsZeroForNullUser() {
        assertEquals(0, messagesService.markAllRead(null, 2));
    }

    @Test
    void broadcastSystemMessageSavesAnnouncement() {
        MessagesBroadcastBo bo = new MessagesBroadcastBo();
        bo.setUserId(10L);
        bo.setTitle(" title ");
        bo.setContent(" content ");
        bo.setUrl("/u");
        bo.setDataId(9001L);
        messagesService.broadcastSystemMessage(bo);
        ArgumentCaptor<Messages> captor = ArgumentCaptor.forClass(Messages.class);
        verify(iMessagesService).save(captor.capture());
        Messages saved = captor.getValue();
        assertEquals(10L, saved.getUserId());
        assertEquals("title", saved.getTitle());
        assertEquals("content", saved.getContent());
        assertEquals(Integer.valueOf(1), saved.getType());
        assertEquals(Integer.valueOf(0), saved.getIsRead());
        assertEquals(Long.valueOf(9001L), saved.getDataId());
    }
}
