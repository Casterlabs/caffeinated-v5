import { writable, type Writable } from 'svelte/store';
import type { Theme } from '../app';

export const ANIMATE_DURATION = 75;
export const ANIMATE_DELAY = 150;

export const isRTL: Writable<boolean> = writable(true);

export const currentTheme: Writable<null | Theme> = writable(null);

window.App.preferences.ui.__stores //
	.svelte('theme')
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
		window.internalSetDarkAppearance(theme.isDark);

		currentTheme.set(theme);
	});
