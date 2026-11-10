import { post } from '@/common/request';

export function fetchUserCenterSummary() {
	return post('/app/pet/user/center/summary', {});
}
