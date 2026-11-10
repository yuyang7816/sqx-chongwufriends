<template>
	<view class="pet-page adopt-form-page">
		<view class="pet-shell adopt-form-shell">
			<view class="form-top-tip">
				<image class="form-top-tip-icon" src="/packageA/static/post/yxIcon.png" mode="widthFix"></image>
				<text class="form-top-tip-text">请认真填写，我们会在 3 个工作日内联系您</text>
			</view>
			<view class="form-slogan-row">
				<text class="form-slogan-icon">🐾</text>
				<text class="form-slogan-text">理性养宠 · 给它一个不离不弃的家</text>
			</view>

			<view class="form-panel basic-panel">
				<view class="form-section-head basic-section-head">
					<view class="basic-section-icon">📋</view>
					<text class="section-title">基本信息</text>
				</view>
				<view class="field-group">
					<text class="field-label">您的姓名 <text class="required-mark">*</text></text>
					<view class="input-shell">
						<input v-model="form.name" class="form-input" placeholder="请输入您的姓名" placeholder-class="form-placeholder" />
					</view>
				</view>
				<view class="field-group">
					<text class="field-label">手机号 <text class="required-mark">*</text></text>
					<view class="input-shell">
						<input v-model="form.phone" class="form-input" type="number" placeholder="请输入手机号" placeholder-class="form-placeholder" />
					</view>
				</view>
				<view class="field-group">
					<text class="field-label">微信号</text>
					<view class="input-shell">
						<input v-model="form.wechat" class="form-input" placeholder="请输入微信号" placeholder-class="form-placeholder" />
					</view>
				</view>
				<view class="field-group">
					<text class="field-label">居住城市 <text class="required-mark">*</text></text>
					<view class="input-shell">
						<input v-model="form.city" class="form-input" placeholder="请填写居住城市" placeholder-class="form-placeholder" />
					</view>
				</view>
				<view class="field-group housing-field-group">
					<text class="field-label">居住条件 <text class="required-mark">*</text></text>
					<view class="housing-group">
						<view v-for="item in housingOptions" :key="item" class="housing-option" @tap="form.housing = item">
							<view class="housing-radio" :class="{ active: form.housing === item }"></view>
							<text class="housing-text">{{ item }}</text>
						</view>
					</view>
				</view>
			</view>

			<view class="form-panel basic-panel adopt-info-panel">
				<view class="form-section-head basic-section-head">
					<view class="basic-section-icon">🐾</view>
					<text class="section-title">领养信息</text>
				</view>
				<view class="field-group">
					<text class="field-label">希望领养的宠物 <text class="required-mark">*</text></text>
					<view class="adopt-upload-shell" @tap="chooseDesiredPetImage">
						<image v-if="form.petImage" class="adopt-upload-preview" :src="form.petImage" mode="aspectFill" @tap.stop="previewDesiredPetImage"></image>
						<view v-else class="adopt-upload-empty">
							<up-icon name="photo" color="#999999" size="26"></up-icon>
						</view>
						<view v-if="form.petImage" class="adopt-upload-delete" @tap.stop="clearDesiredPetImage">×</view>
					</view>
				</view>
				<view class="option-block">
					<text class="field-label">是否有养宠经验 <text class="required-mark">*</text></text>
					<view class="plain-radio-row">
						<view class="plain-radio-option" @tap="form.hasExperience = true">
							<view class="plain-radio" :class="{ active: form.hasExperience }"></view>
							<text class="plain-radio-text">是</text>
						</view>
						<view class="plain-radio-option" @tap="form.hasExperience = false">
							<view class="plain-radio" :class="{ active: !form.hasExperience }"></view>
							<text class="plain-radio-text">否</text>
						</view>
					</view>
				</view>
				<view class="option-block">
					<text class="field-label">目前是否有其他宠物 <text class="required-mark">*</text></text>
					<view class="plain-radio-row">
						<view class="plain-radio-option" @tap="form.hasOtherPet = true">
							<view class="plain-radio" :class="{ active: form.hasOtherPet }"></view>
							<text class="plain-radio-text">是</text>
						</view>
						<view class="plain-radio-option" @tap="form.hasOtherPet = false">
							<view class="plain-radio" :class="{ active: !form.hasOtherPet }"></view>
							<text class="plain-radio-text">否</text>
						</view>
					</view>
				</view>
				<view class="field-group">
					<text class="field-label">如果是，请说明</text>
					<view class="textarea-shell">
						<textarea v-model="form.otherPetIntro" class="form-textarea" placeholder="如有其他宠物，请简单介绍种类和数量" placeholder-class="form-placeholder"></textarea>
					</view>
				</view>
			</view>

			<view class="form-panel basic-panel adopt-condition-panel">
				<view class="form-section-head basic-section-head">
					<view class="basic-section-icon">🏡</view>
					<text class="section-title">养宠条件评估</text>
				</view>
				<view class="option-block">
					<text class="field-label">您的工作状况 <text class="required-mark">*</text></text>
					<view class="plain-radio-grid">
						<view v-for="item in workOptions" :key="item" class="plain-radio-option is-grid" @tap="form.workStatus = item">
							<view class="plain-radio" :class="{ active: form.workStatus === item }"></view>
							<text class="plain-radio-text">{{ item }}</text>
						</view>
					</view>
				</view>
				<view class="option-block">
					<text class="field-label">每天可陪伴宠物的时间 <text class="required-mark">*</text></text>
					<view class="plain-radio-grid time-radio-grid">
						<view v-for="item in timeOptions" :key="item" class="plain-radio-option is-grid" @tap="form.companionTime = item">
							<view class="plain-radio" :class="{ active: form.companionTime === item }"></view>
							<text class="plain-radio-text">{{ item }}</text>
						</view>
					</view>
				</view>
				<view class="option-block">
					<text class="field-label">家庭中是否有老人或小孩 <text class="required-mark">*</text></text>
					<view class="plain-radio-grid family-radio-grid">
						<view v-for="item in familyOptions" :key="item" class="plain-radio-option is-grid" @tap="form.familyStatus = item">
							<view class="plain-radio" :class="{ active: form.familyStatus === item }"></view>
							<text class="plain-radio-text">{{ item }}</text>
						</view>
					</view>
				</view>
				<view class="field-group">
					<text class="field-label">您认为养宠最大的挑战是？</text>
					<view class="textarea-shell">
						<textarea v-model="form.challenge" class="form-textarea" placeholder="时间、经济、空间等" placeholder-class="form-placeholder"></textarea>
					</view>
				</view>
			</view>

			<view class="form-panel basic-panel promise-panel">
				<view class="form-section-head basic-section-head">
					<view class="basic-section-icon">💛</view>
					<text class="section-title">养宠承诺</text>
				</view>
				<view class="promise-box">
					<view class="promise-text-row">
						<text class="promise-inline-icon">🐾</text>
						<text class="promise-text">我承诺：我会给它一个温暖的家，善待它、陪伴它，不离不弃。无论搬家、工作变动、生活变化，我都不会放弃它，因为它不只是宠物，更是家人。💛</text>
					</view>
				</view>
				<view class="checkbox-row promise-check-row" @tap="form.promise = !form.promise">
					<view class="fake-check" :class="{ active: form.promise }">
						<text v-if="form.promise" class="fake-check-mark">✓</text>
					</view>
					<text class="checkbox-text">我已阅读并同意以上承诺</text>
				</view>
				<view class="checkbox-row promise-check-row" @tap="form.visitBack = !form.visitBack">
					<view class="fake-check" :class="{ active: form.visitBack }">
						<text v-if="form.visitBack" class="fake-check-mark">✓</text>
					</view>
					<text class="checkbox-text">我了解领养后需要定期回访</text>
				</view>
			</view>

			<view class="pet-button form-submit-btn" @tap="submitForm">
				<text>提交申请</text>
				<text class="form-submit-icon">📮</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import {
	reactive,
	ref
} from 'vue';
import { submitAdoptApplication, fetchAdoptPetInfo } from '@/common/api/adopt.js';
import { uploadImage } from '@/common/api/file.js';
import { resolveMediaUrl } from '@/common/media.js';
import { ensureLogin } from '@/common/auth-guard.js';
import {
	onLoad
} from '@dcloudio/uni-app';

const petId = ref(1);
const petName = ref('豆包');
const housingOptions = ['自有住房', '租房', '其他'];
const workOptions = ['全职居家', '定时上下班', '经常出差', '学生'];
const timeOptions = ['1 小时以内', '1-3 小时', '3-5 小时', '5 小时以上'];
const familyOptions = ['有老人', '有小孩', '有老人和小孩', '都没有'];

const form = reactive({
	name: '',
	phone: '',
	wechat: '',
	city: '',
	housing: '自有住房',
	petName: '',
	petImage: '',
	hasExperience: true,
	hasOtherPet: false,
	otherPetIntro: '',
	workStatus: '定时上下班',
	companionTime: '3-5 小时',
	familyStatus: '都没有',
	challenge: '',
	promise: true,
	visitBack: true
});

onLoad(async option => {
	petId.value = Number(option.id || 0);
	if (!petId.value) return;
	try {
		const data = await fetchAdoptPetInfo(petId.value);
		if (data) {
			petName.value = data.name || '';
			form.petName = petName.value;
			if (data.avatarUrl) {
				form.petImage = resolveMediaUrl(data.avatarUrl);
			}
		}
	} catch (e) {
		uni.showToast({
			title: '加载宠物信息失败',
			icon: 'none'
		});
	}
});

const chooseDesiredPetImage = () => {
	uni.chooseImage({
		count: 1,
		sizeType: ['compressed'],
		sourceType: ['album', 'camera'],
		success: res => {
			form.petImage = res.tempFilePaths?.[0] || '';
		}
	});
};

const previewDesiredPetImage = () => {
	if (!form.petImage) return;
	uni.previewImage({
		current: form.petImage,
		urls: [form.petImage]
	});
};

const clearDesiredPetImage = () => {
	form.petImage = '';
};

const isPhoneValid = (phone) => /^1\d{10}$/.test(String(phone || '').trim());

const submitForm = async () => {
	if (!form.name || !form.phone || !form.city || !form.petImage || !form.promise || !form.visitBack) {
		uni.showToast({
			title: '请完善必填信息并勾选承诺',
			icon: 'none'
		});
		return;
	}
	if (!isPhoneValid(form.phone)) {
		uni.showToast({ title: '请输入正确手机号', icon: 'none' });
		return;
	}
	if (!ensureLogin()) return;
	try {
		uni.showLoading({ title: '提交中' });
		const petImageUrl = await uploadImage(form.petImage);
		await submitAdoptApplication({
			petId: petId.value,
			realName: form.name,
			phone: form.phone,
			city: form.city,
			wechat: form.wechat,
			housing: form.housing,
			hasExperience: form.hasExperience,
			hasOtherPet: form.hasOtherPet,
			otherPetIntro: form.otherPetIntro,
			workStatus: form.workStatus,
			companionTime: form.companionTime,
			familyStatus: form.familyStatus,
			challenge: form.challenge,
			petImage: petImageUrl,
			petName: form.petName
		});
		uni.hideLoading();
		uni.showToast({
			title: '申请已提交',
			icon: 'success'
		});
		setTimeout(() => {
			uni.navigateTo({ url: '/packageA/adopt/mine' });
		}, 500);
	} catch (e) {
		uni.hideLoading();
		const reason = e?.msg || e?.message || '提交失败，请稍后重试';
		uni.showModal({
			title: '提交失败',
			content: String(reason),
			showCancel: false,
			confirmText: '我知道了'
		});
	}
};
</script>

<style lang="scss">
.adopt-form-page {
	background: linear-gradient(180deg, #fff7ef 0%, #f8efe2 100%);
}

.adopt-form-shell {
	padding-bottom: 36rpx;
}

.form-top-tip {
	margin-top: 8rpx;
	padding: 14rpx 18rpx;
	border-radius: 22rpx;
	background: #F3F4EB;
	
	border: 2rpx dashed #bfd9c8;
	display: flex;
	align-items: center;
	gap: 10rpx;
}

.form-top-tip-icon {
	width: 45rpx;
	height: 45rpx;
	
}

.form-top-tip-text {
	font-size: 21rpx;
	color: #7f8f80;
}

.form-slogan-row {
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 8rpx;
	margin-top: 12rpx;
	margin-bottom: 2rpx;
	padding: 0 12rpx;
}

.form-slogan-icon {
	font-size: 18rpx;
	line-height: 1;
}

.form-slogan-text {
	font-size: 20rpx;
	line-height: 1.5;
	color: #b77b4d;
	text-align: center;
}

.form-panel {
	position: relative;
	margin-top: 22rpx;
	padding: 28rpx 24rpx;
	border-radius: 30rpx;
	// background: rgba(255, 253, 249, 0.98);
	border: 2rpx solid rgba(236, 215, 191, 0.75);
	box-shadow: 0 14rpx 34rpx rgba(171, 123, 79, 0.08);
}

.basic-panel {
	padding: 26rpx 22rpx 24rpx;
	border-style: dashed;
	border-color: rgba(226, 202, 172, 0.95);
	box-shadow: none;
	background: rgba(255, 251, 245, 0.98);
}

.form-section-head {
	display: flex;
	align-items: center;
	gap: 12rpx;
	margin-bottom: 22rpx;
}

.basic-section-head {
	margin-bottom: 20rpx;
	gap: 10rpx;
}

.basic-section-icon {
	width: 34rpx;
	height: 34rpx;
	border-radius: 10rpx;
	background: #f8efe3;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 22rpx;
	line-height: 1;
	flex-shrink: 0;
}

.form-section-icon {
	font-size: 28rpx;
	line-height: 1;
}

.section-title {
	font-size: 30rpx;
	font-weight: 700;
	color: #6d4c38;
}

.field-group,
.option-block {
	margin-bottom: 20rpx;
	display: flex;
	flex-direction: column;
	gap: 12rpx;
}

.basic-panel .field-group {
	margin-bottom: 18rpx;
	gap: 10rpx;
}

.adopt-info-panel .field-group,
.adopt-info-panel .option-block {
	margin-bottom: 24rpx;
	gap: 12rpx;
}

.adopt-condition-panel .field-group,
.adopt-condition-panel .option-block {
	margin-bottom: 24rpx;
	gap: 12rpx;
}

.promise-panel {
	padding-bottom: 22rpx;
}

.field-group:last-child,
.option-block:last-child {
	margin-bottom: 0;
}

	.field-label {
		font-size: 24rpx;
		font-weight: 600;
		color: #866652;
	}

.field-label .required-mark {
	color:#f35100;
	
}

.basic-panel .field-label {
	font-size: 26rpx;
	font-weight: 500;
	color: #6f5543;
	margin-top: 10rpx;
}

.input-shell,
.textarea-shell {
	padding: 0 24rpx;
	border-radius: 20rpx;
	background: linear-gradient(180deg, #f8efe4 0%, #f5eadb 100%);
	overflow: hidden;
}

.input-shell {
	height: 78rpx;
	display: flex;
	align-items: center;
}

.basic-panel .input-shell {
	height: 80rpx;
	padding: 0 22rpx;
	border-radius: 18rpx;
	border: 1rpx solid #EDE0D4;
	background: linear-gradient(180deg, #f8f1e7 0%, #f5ede2 100%);
}

.adopt-upload-shell {
	position: relative;
	height:280rpx;
	border-radius:24rpx;
	border: 1rpx solid #ede0d4;
	background: linear-gradient(180deg, #f8f1e7 0%, #f5ede2 100%);
	overflow: hidden;
	display: flex;
	align-items: center;
	justify-content: center;
}

.adopt-upload-empty {
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 12rpx;
	width: 100%;
	height: 100%;
}



.adopt-upload-preview {
	width: 100%;
	height: 100%;
	display: block;
}

.adopt-upload-delete {
	position: absolute;
	top: 10rpx;
	right: 10rpx;
	width: 34rpx;
	height: 34rpx;
	border-radius: 50%;
	background: rgba(255, 248, 240, 0.92);
	color: #a3754f;
	font-size: 24rpx;
	line-height: 34rpx;
	text-align: center;
	box-shadow: 0 6rpx 12rpx rgba(163, 117, 79, 0.12);
}

.textarea-shell {
	padding-top: 20rpx;
	padding-bottom: 20rpx;
}

.basic-panel .textarea-shell {
	padding: 18rpx 22rpx;
	border-radius: 18rpx;
	background: linear-gradient(180deg, #f8f1e7 0%, #f5ede2 100%);
}

.form-input {
	width: 100%;
	height: 78rpx;
	font-size: 26rpx;
	color: #6d4c38;
}

.basic-panel .form-input {
	height: 72rpx;
	font-size: 24rpx;
	color: #b7a796;
}

.form-textarea {
	width: 100%;
	min-height: 120rpx;
	font-size: 26rpx;
	line-height: 1.8;
	color: #6d4c38;
}

.basic-panel .form-textarea {
	font-size: 24rpx;
	min-height: 104rpx;
	color: #b7a796;
}

.adopt-info-panel .form-textarea {
	min-height: 118rpx;
}

.adopt-condition-panel .form-textarea {
	min-height: 116rpx;
}

.form-placeholder {
	color: #d2c6b8;
	font-size: 24rpx;
}

.radio-group {
	display: flex;
	flex-wrap: wrap;
	gap: 14rpx;
}

.radio-group.small {
	gap: 12rpx;
}

.basic-panel .radio-group {
	gap: 12rpx;
}

.plain-radio-row {
	display: flex;
	align-items: center;
	gap: 36rpx;
	padding-top: 2rpx;
}

.plain-radio-option {
	display: inline-flex;
	align-items: center;
	gap: 12rpx;
}

.plain-radio-option.is-grid {
	min-width: 180rpx;
	margin-right: 6rpx;
}

.plain-radio-grid {
	display: flex;
	flex-wrap: wrap;
	row-gap: 22rpx;
	column-gap: 18rpx;
	padding-top: 2rpx;
}

.time-radio-grid,
.family-radio-grid {
	row-gap: 20rpx;
}

.plain-radio {
	width: 26rpx;
	height: 26rpx;
	border-radius: 50%;
	border: 2rpx solid #d3bda2;
	background: #fffaf4;
	position: relative;
	flex-shrink: 0;
}

.plain-radio.active {
	border-color: #c79662;
}

.plain-radio.active::after {
	content: '';
	position: absolute;
	left: 50%;
	top: 50%;
	width: 10rpx;
	height: 10rpx;
	border-radius: 50%;
	background: #c79662;
	transform: translate(-50%, -50%);
}

.plain-radio-text {
	font-size: 24rpx;
	color: #6f5543;
	line-height: 1;
}

.housing-field-group {
	margin-bottom: 0;
}

.housing-group {
	display: flex;
	flex-wrap: wrap;
	gap: 26rpx;
	padding: 20rpx 0 2rpx;
}

.housing-option {
	display: inline-flex;
	align-items: center;
	gap: 10rpx;
}

.housing-radio {
	width: 24rpx;
	height: 24rpx;
	border-radius: 50%;
	border: 2rpx solid #ccb59a;
	background: #fffdf8;
	position: relative;
	flex-shrink: 0;
}

.housing-radio.active {
	border-color: #c79662;
}

.housing-radio.active::after {
	content: '';
	position: absolute;
	left: 50%;
	top: 50%;
	width: 10rpx;
	height: 10rpx;
	border-radius: 50%;
	background: #c79662;
	transform: translate(-50%, -50%);
}

.housing-text {
	font-size: 26rpx;
	color: #6f5543;
	line-height: 1;
}

.radio-chip {
	padding: 0 24rpx;
	height: 58rpx;
	line-height: 58rpx;
	border-radius: 999rpx;
	background: #fff8ef;
	border: 2rpx solid #ecd7bf;
	font-size: 24rpx;
	color: #8a725c;
}

.basic-panel .radio-chip {
	height: 52rpx;
	line-height: 52rpx;
	padding: 0 20rpx;
	font-size: 23rpx;
	background: #fcf5ea;
}

.radio-chip.active {
	background: #fff1db;
	border-color: #d7aa77;
	color: #8b613d;
	font-weight: 700;
}

.promise-box {
	padding: 28rpx 24rpx;
	border-radius: 22rpx;
	background: linear-gradient(180deg, #fff9f0 0%, #fff5e8 100%);
	border: 2rpx dashed rgba(228, 204, 176, 0.92);
	margin-bottom: 22rpx;
}

.basic-panel .promise-box {
	background: linear-gradient(180deg, #fff8ec 0%, #fff2e0 100%);
}

.promise-text-row {
	display: flex;
	align-items: flex-start;
	gap: 10rpx;
}

.promise-inline-icon {
	font-size: 20rpx;
	line-height: 1.7;
	color: #b97c48;
	flex-shrink: 0;
}

.promise-text {
	font-size: 24rpx;
	line-height: 1.95;
	color: #856c58;
}

.promise-panel .promise-text {
	font-size: 23rpx;
	line-height: 1.9;
}

.checkbox-row {
	display: flex;
	align-items: center;
	gap: 16rpx;
	margin-bottom: 18rpx;
	padding: 4rpx 0;
}

.promise-check-row {
	margin-bottom: 14rpx;
	padding: 2rpx 0;
}

.checkbox-row:last-child {
	margin-bottom: 0;
}

.fake-check {
	width: 30rpx;
	height: 30rpx;
	border-radius: 8rpx;
	border: 2rpx solid #d7c3aa;
	background: #fffaf4;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
}

.fake-check.active {
	background: #fff8ef;
	border-color: #c9a277;
}

.fake-check-mark {
	font-size: 20rpx;
	color: #be8550;
	line-height: 1;
}

.checkbox-text {
	font-size: 23rpx;
	color: #7b6554;
	line-height: 1.6;
}

.form-submit-btn {
	margin-top: 28rpx;
	height: 92rpx;
	border-radius: 999rpx;
	background: linear-gradient(135deg, #cea06f, #c28a55);
	box-shadow: 0 16rpx 28rpx rgba(194, 138, 85, 0.24);
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 10rpx;
	padding: 0 24rpx;
	font-size: 28rpx;
}

.form-submit-icon {
	font-size: 22rpx;
	line-height: 1;
}
</style>
