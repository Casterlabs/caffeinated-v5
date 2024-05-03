<script lang="ts">
	import './app.css';

	import iconsHook from '$lib/icons';
	import { onMount } from 'svelte';
	import { currentTheme } from '$lib/app';

	let isDarkTheme = true;
	let intermediate: HTMLElement;

	onMount(iconsHook);

	currentTheme.subscribe((theme) => {
		if (!theme) return;

		for (const [idx, c] of Object.entries(theme.baseScale)) {
			intermediate.style.setProperty(`--baseSDR-${parseInt(idx) + 1}`, c);
		}
		for (const [idx, c] of Object.entries(theme.accentScale)) {
			intermediate.style.setProperty(`--accentSDR-${parseInt(idx) + 1}`, c);
		}
		if (theme.baseScaleP3) {
			for (const [idx, c] of Object.entries(theme.baseScaleP3)) {
				intermediate.style.setProperty(`--baseP3-${parseInt(idx) + 1}`, c);
			}
		}
		if (theme.accentScaleP3) {
			for (const [idx, c] of Object.entries(theme.accentScaleP3)) {
				intermediate.style.setProperty(`--accentP3-${parseInt(idx) + 1}`, c);
			}
		}
	});
</script>

<!--
	The sites's theming is handled with data-theme-dark in colors.css.
	All of the css files to make this happen are imported above.
-->

<div bind:this={intermediate} id="css-intermediate" class="relative w-full h-full bg-base-1 text-base-12 overflow-auto" data-theme-dark={isDarkTheme}>
	<slot />
</div>

<style>
	#css-intermediate {
		--base-1: var(--baseSDR-1);
		--base-2: var(--baseSDR-2);
		--base-3: var(--baseSDR-3);
		--base-4: var(--baseSDR-4);
		--base-5: var(--baseSDR-5);
		--base-6: var(--baseSDR-6);
		--base-7: var(--baseSDR-7);
		--base-8: var(--baseSDR-8);
		--base-9: var(--baseSDR-9);
		--base-10: var(--baseSDR-10);
		--base-11: var(--baseSDR-11);
		--base-12: var(--baseSDR-12);
		--accent-1: var(--accentSDR-1);
		--accent-2: var(--accentSDR-2);
		--accent-3: var(--accentSDR-3);
		--accent-4: var(--accentSDR-4);
		--accent-5: var(--accentSDR-5);
		--accent-6: var(--accentSDR-6);
		--accent-7: var(--accentSDR-7);
		--accent-8: var(--accentSDR-8);
		--accent-9: var(--accentSDR-9);
		--accent-10: var(--accentSDR-10);
		--accent-11: var(--accentSDR-11);
		--accent-12: var(--accentSDR-12);
	}

	@supports (color: color(display-p3 1 1 1)) {
		@media (color-gamut: p3) {
			#css-intermediate {
				--base-1: var(--baseP3-1, var(--baseSDR-1)) !important;
				--base-2: var(--baseP3-2, var(--baseSDR-2)) !important;
				--base-3: var(--baseP3-3, var(--baseSDR-3)) !important;
				--base-4: var(--baseP3-4, var(--baseSDR-4)) !important;
				--base-5: var(--baseP3-5, var(--baseSDR-5)) !important;
				--base-6: var(--baseP3-6, var(--baseSDR-6)) !important;
				--base-7: var(--baseP3-7, var(--baseSDR-7)) !important;
				--base-8: var(--baseP3-8, var(--baseSDR-8)) !important;
				--base-9: var(--baseP3-9, var(--baseSDR-9)) !important;
				--base-10: var(--baseP3-10, var(--baseSDR-10)) !important;
				--base-11: var(--baseP3-11, var(--baseSDR-11)) !important;
				--base-12: var(--baseP3-12, var(--baseSDR-12)) !important;
				--accent-1: var(--accentP3-1, var(--accentSDR-1)) !important;
				--accent-2: var(--accentP3-2, var(--accentSDR-2)) !important;
				--accent-3: var(--accentP3-3, var(--accentSDR-3)) !important;
				--accent-4: var(--accentP3-4, var(--accentSDR-4)) !important;
				--accent-5: var(--accentP3-5, var(--accentSDR-5)) !important;
				--accent-6: var(--accentP3-6, var(--accentSDR-6)) !important;
				--accent-7: var(--accentP3-7, var(--accentSDR-7)) !important;
				--accent-8: var(--accentP3-8, var(--accentSDR-8)) !important;
				--accent-9: var(--accentP3-9, var(--accentSDR-9)) !important;
				--accent-10: var(--accentP3-10, var(--accentSDR-10)) !important;
				--accent-11: var(--accentP3-11, var(--accentSDR-11)) !important;
				--accent-12: var(--accentP3-12, var(--accentSDR-12)) !important;
			}
		}
	}
</style>
