import { get, post } from '@/common/request';

export function fetchCities() {
	return get('/app/pet/home/cities', {}, { needAuth: false });
}

export function fetchStores(cityId) {
	return get('/app/pet/home/stores', { cityId }, { needAuth: false });
}

export function fetchPets(storeId, petType) {
	const params = { storeId };
	if (petType) params.petType = petType;
	return get('/app/pet/home/pets', params, { needAuth: false });
}

export function fetchBanners(positionCode = 'home') {
	return get('/app/pet/home/banners', { positionCode }, { needAuth: false });
}

export function fetchHomeConfig() {
	return get('/app/pet/home/config', {}, { needAuth: false });
}

export function trackBannerClick(bannerId) {
	return post('/app/pet/home/banner/click', { bannerId }, { needAuth: false });
}
