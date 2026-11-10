package com.sqx.app.pet.common.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sqx.app.pet.common.domain.entity.SensitiveWord;
import com.sqx.app.pet.common.db.service.ISensitiveWordService;
import com.sqx.app.pet.common.enums.PetError;
import com.sqx.common.core.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SensitiveWordService {

    @Resource
    private ISensitiveWordService iSensitiveWordService;

    public void checkText(String text, String scene) {
        if (StringUtils.isBlank(text)) {
            return;
        }
        List<String> words = loadWords();
        for (String word : words) {
            if (text.contains(word)) {
                log.warn("[敏感词] scene={} hit={}", scene, word);
                throw new BizException(PetError.SENSITIVE_WORD);
            }
        }
    }

    private List<String> loadWords() {
        QueryWrapper<SensitiveWord> qw = new QueryWrapper<>();
        qw.lambda().eq(SensitiveWord::getStatus, 1);
        return iSensitiveWordService.list(qw).stream()
                .map(SensitiveWord::getWord)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
    }
}
