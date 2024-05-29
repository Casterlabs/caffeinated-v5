<script lang="ts">
	import { onDestroy } from 'svelte';

	let history: any[] = [];

	(async () => {
		const h = await window.App.koi.history;
		history = h;
	})();

	// @ts-ignore
	const unregisterEventsListener = window.Bridge.on('koi-event', (e) => {
		history.push(e);
		history = history;
	});
	onDestroy(unregisterEventsListener);
</script>

{#each history as event}
	{#if event.event_type == 'RICH_MESSAGE'}
		<pre class="block">{event.sender.displayname}: {@html event.html}</pre>
	{:else}
		<pre class="block">{event.event_type}</pre>
	{/if}
{/each}
