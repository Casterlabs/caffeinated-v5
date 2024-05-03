import { redirect } from '@sveltejs/kit';
import type { PageLoad } from '../$types';

export const load = (async () => {
	throw redirect(302, '/settings/accounts');
}) satisfies PageLoad;
