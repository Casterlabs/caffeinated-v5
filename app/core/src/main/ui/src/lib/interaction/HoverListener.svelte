<script lang="ts">
	import { createEventDispatcher, onMount } from 'svelte';

	const dispatch = createEventDispatcher();

	export let ignoreFocusState = false;
	export let focused = false;
	let container: HTMLDivElement;

	function isDescendant(parent: Node, child: Node) {
		if (child == null) {
			return false;
		}

		let node: null | Node = child;
		while (node != null) {
			if (node == parent) {
				return true;
			}
			node = node.parentNode;
		}

		return false;
	}

	function lostFocus(e: MouseEvent) {
		if (!focused && !ignoreFocusState) return;
		const target = e.relatedTarget as HTMLElement;

		if (target?.hasAttribute('focus-ignore')) {
			return;
		}

		// If the new focused element is NOT one of ours.
		if (!isDescendant(container, target)) {
			focused = false;
			dispatch('lostfocus');
		}
	}

	function gainedFocus() {
		if (focused && !ignoreFocusState) return;
		focused = true;
		dispatch('gainedfocus');
	}
</script>

<!-- svelte-ignore a11y-click-events-have-key-events -->
<!-- svelte-ignore a11y-no-static-element-interactions -->
<div
	style="display: contents;"
	bind:this={container}
	on:mouseenter={gainedFocus}
	on:mouseleave={lostFocus}
	on:click={gainedFocus}
>
	<slot />
</div>
