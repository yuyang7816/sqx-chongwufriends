import { post } from '@/common/request';

export function fetchArchiveInfo(id) {
	return post('/app/pet/archive/info', { id }, { needAuth: false });
}
