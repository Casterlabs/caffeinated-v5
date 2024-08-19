<script lang="ts">
	import { onDestroy } from 'svelte';

	let history: KoiHistoryEntry[] = [];
	let scrollContainer: HTMLDivElement;

	(async () => {
		const h = await window.App.koi.history.getHistoryAtOrBeforeTimestamp(Date.now());
		history = h;
	})();

	// @ts-ignore
	const unregisterId = window.saucer.messages.onMessage(({ type, data: event }) => {
		if (type == 'koi-event') {
			history.push({ uuid: 'invalid', event });
			history = history; // refresh
		}
	});
	onDestroy(() => window.saucer.messages.off(unregisterId));
</script>

<div
	bind:this={scrollContainer}
	class="h-full overflow-x-auto overflow-y-none"
	style="transform: scaleY(-1);"
	on:scroll|preventDefault
	on:wheel|preventDefault={(e) => {
		scrollContainer.scrollTop -= e.deltaY / 2;
	}}
>
	<div class="h-fit" style="transform: scaleY(-1);">
		<button
			on:click={async () => {
				const beforeOrAt = new Date(history[0]?.event?.timestamp || Date.now()).getTime();
				let older = await window.App.koi.history.getHistoryAtOrBeforeTimestamp(beforeOrAt);

				older = older.filter(({ uuid }) => {
					for (const { uuid: alreadyUuid } of history) {
						if (uuid == alreadyUuid) return false;
					}
					return true;
				});
				history = [...older, ...history];
			}}
		>
			Load more older messages
		</button>

		{#each history as { event }}
			{#if event.event_type == 'RICH_MESSAGE'}
				<pre class="block">{event.sender.displayname}: {@html event.html}</pre>
			{:else}
				<pre class="block">{event.event_type}</pre>
			{/if}
		{/each}
	</div>
</div>
