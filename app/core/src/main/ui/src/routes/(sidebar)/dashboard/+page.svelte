<script lang="ts">
	import { onDestroy } from 'svelte';

	let history: any[] = [];

	(async () => {
		const h = await window.App.koi.history;
		history = h;
	})();

	// @ts-ignore
	const unregisterEventsListener = window.saucer.messages.onMessage(({ type, data: event }) => {
		if (type == 'koi-event') {
			history.push(event);
			history = history;
		}
	});
	onDestroy(() => window.saucer.messages.off(unregisterEventsListener));
</script>

{#each history as event}
	{#if event.event_type == 'RICH_MESSAGE'}
		<pre class="block">{event.sender.displayname}: {@html event.html}</pre>
	{:else}
		<pre class="block">{event.event_type}</pre>
	{/if}
{/each}
