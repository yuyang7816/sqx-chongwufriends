import { post } from '@/common/request';
import { getIdempotentKey } from '@/common/auth-guard';

export function fetchCouponList(status) {
	return post('/app/pet/coupon/list', status ? { status } : {});
}

export function useCoupon(couponId, orderId) {
	return post('/app/pet/mall/order/coupon/use', {
		couponId,
		orderId,
		idempotentKey: getIdempotentKey(`coupon_use_${couponId}`)
	});
}
