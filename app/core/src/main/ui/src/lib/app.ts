import { writable, type Writable } from 'svelte/store';
import type { SaucerIPCObject, Theme } from '../app';

export const ANIMATE_DURATION = 75;
export const ANIMATE_DELAY = 150;

export const isRTL: Writable<boolean> = writable(true);

export const currentTheme: Writable<null | Theme> = writable(null);

const writableCache: { [key: string]: Writable<null | any> } = {};
export function svelte(object: string, field: string) {
	if (writableCache[object]) {
		return writableCache[object];
	}

	// @ts-ignore
	let root: SaucerIPCObject = window;
	try {
		const store = writable(null);

		for (const part of object.split(".")) {
			// @ts-ignore
			root = root[part];
		}

		root.onMutate(field, store.set);
		writableCache[object] = store;

		// @ts-ignore
		root[field].then(store.set);

		return store;
	} catch (e) {
		console.debug(e, root, object, field);
		throw "Probably could not find a root, you supplied: " + object;
	}
}

svelte("App.preferences.ui", "theme") //
	.subscribe(async (themeId) => {
		if (themeId == null) return;

		let theme = (await window.App.themes).map[themeId];
		if (!theme) {
			// Fallback...
			alert('Invalid theme ID! Falling back...');
			theme = (await window.App.themes).map['co.casterlabs.nqp_dark'];
		}

		// @ts-ignore
		// See AppInterface.java
		// window.internalSetDarkAppearance(theme.isDark);

		console.log(theme);
		currentTheme.set(theme);
	});
