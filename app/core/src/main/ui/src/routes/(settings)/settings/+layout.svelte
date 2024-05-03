<script lang="ts">
	import { page } from '$app/stores';
	import { ANIMATE_DELAY, ANIMATE_DURATION } from '$lib/app.js';
	import { fade } from 'svelte/transition';

	export let data;

	const LINKS = [
		{
			icon: 'outline/users',
			name: 'Accounts',
			href: '/settings/accounts'
		},
		{
			icon: 'outline/paint-brush',
			name: 'Appearance',
			href: '/settings/appearance'
		},
		{
			icon: 'outline/puzzle-piece',
			name: 'Plugins',
			href: '/settings/plugins'
		},
		{
			icon: 'outline/lifebuoy',
			name: 'Help & About',
			href: '/settings/about'
		}
	];
</script>

<div class="fixed inset-0 bg-base-1 flex flex-col h-screen" in:fade={{ duration: ANIMATE_DURATION, delay: ANIMATE_DELAY }} out:fade={{ duration: ANIMATE_DURATION }}>
	<nav class="bg-base-2 border-b border-base-3 shadow-sm pt-3 overflow-x-auto">
		<ul class="flex flex-row space-x-8 w-fit">
			<li class="inline-block">
				<a href="/dashboard" class="ml-4 -mr-3 block text-base-12 rounded-md p-1 hover:bg-base-3 transition-colors overflow-hidden" title="Go back">
					<div class="flex flex-row space-x-2">
						<icon class="flex-0 ml-1 w-6 h-6" data-icon="outline/arrow-left" />
						<span class="sr-only"> Go back </span>
					</div>
				</a>
			</li>

			{#each LINKS as link}
				{@const isCurrentPage = $page.url.pathname.startsWith(link.href)}
				<li class="inline-block">
					<a
						aria-disabled={isCurrentPage}
						href={link.href}
						class="px-2 block text-base-12 p-1 pb-3 border-[transparent] border-b-2 transition-colors overflow-hidden hover:border-accent-5"
						class:border-[var(--accent-8)]={isCurrentPage}
					>
						<div class="flex flex-row space-x-2">
							<icon class="flex-0 w-6 h-6" data-icon={link.icon} />
							<span class="flex-1 translate-y-0.5 font-semibold text-sm whitespace-nowrap">
								{link.name}
							</span>
						</div>
					</a>
				</li>
			{/each}
		</ul>
	</nav>

	<div class="flex-1 px-2 mt-2 relative w-full">
		{#key data.pathname}
			<div class="absolute inset-0 overflow-y-auto" in:fade={{ duration: ANIMATE_DURATION, delay: ANIMATE_DELAY }} out:fade={{ duration: ANIMATE_DURATION }}>
				<slot />
			</div>
		{/key}
	</div>
</div>
