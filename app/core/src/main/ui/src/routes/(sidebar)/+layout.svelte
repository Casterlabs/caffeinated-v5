<script lang="ts">
	import { page } from '$app/stores';
	import { ANIMATE_DELAY, ANIMATE_DURATION, currentTheme, svelte } from '$lib/app';
	import type { Writable } from 'svelte/store';
	import { fade } from 'svelte/transition';
	import type { KoiAccount } from '../../app.js';

	const accounts: Writable<KoiAccount[]> = svelte('App.koi', 'accounts');

	export let data;

	const LINKS = [
		{
			icon: 'outline/squares-2x2',
			name: 'Dashboard',
			href: '/dashboard'
		},
		{
			icon: 'outline/code-bracket',
			name: 'Chat Bot',
			href: '/chat-bot'
		},
		{
			icon: 'outline/window',
			name: 'Docks',
			href: '/docks'
		},
		{
			icon: 'outline/bell-alert',
			name: 'Widgets & Alerts',
			href: '/widgets'
		}
	];
</script>

<div class="fixed inset-0 flex flex-row text-left" in:fade={{ duration: ANIMATE_DURATION, delay: ANIMATE_DELAY }} out:fade={{ duration: ANIMATE_DURATION }}>
	<nav class="w-20 hover:w-56 bg-base-2 border-r border-base-3 px-4 pb-4 pt-6 flex flex-col transition-[width]">
		<!-- <img class="px-2 block" src={$currentTheme?.wordmarkDataUri} alt="" /> -->

		<ul class="flex-1 overflow-x-hidden overflow-y-auto space-y-2">
			{#each LINKS as link}
				{@const isCurrentPage = $page.url.pathname.startsWith(link.href)}
				<li>
					<a aria-disabled={isCurrentPage} href={link.href} class="block text-base-12 rounded-md p-2 pr-4 hover:bg-base-3 transition-colors overflow-hidden" class:bg-base-4={isCurrentPage}>
						<div class="flex flex-row space-x-4 w-56">
							<icon class="flex-0 ml-1 w-6 h-6" data-icon={link.icon} />
							<span class="flex-1 translate-y-0.5 font-semibold text-sm">
								{link.name}
							</span>
						</div>
					</a>
				</li>
			{/each}
		</ul>

		{#if true}
			{@const accounts = Object.values($accounts || {}).filter((a) => a.isAlive)}
			{@const isCurrentPage = $page.url.pathname.startsWith('/settings')}
			{#if accounts.length == 0}
				<a href="/settings" class="rounded-lg h-12 overflow-hidden text-sm font-semibold leading-6 text-white hover:bg-base-3 transition-colors" class:bg-base-4={isCurrentPage}>
					<span class="sr-only"> Open Settings </span>
					<div class="py-2 space-y-4 transition-transform">
						<div aria-hidden="true" class="w-fit mx-2 flex items-center gap-x-3">
							<icon class="w-8 h-8" data-icon="outline/cog" />
							<span> Settings </span>
						</div>
					</div>
				</a>
			{:else}
				<a href="/settings" class="rounded-lg h-12 overflow-hidden text-sm font-semibold leading-6 text-white hover:bg-base-3 transition-colors" class:bg-base-4={isCurrentPage}>
					<span class="sr-only"> Open Settings </span>
					<div class="hover:-translate-y-1/2 py-2 space-y-4 transition-transform">
						<div aria-hidden="true" class="w-fit mx-2 flex items-center gap-x-3">
							<img class="h-8 w-8 rounded-full bg-[black]" src={accounts[0].profile.image_link} alt="" />
							<span> {accounts[0].profile.displayname} </span>
						</div>
						<div aria-hidden="true" class="w-fit mx-2 flex items-center gap-x-3">
							<icon class="w-8 h-8" data-icon="outline/cog" />
							<span> Settings </span>
						</div>
					</div>
				</a>
			{/if}
		{/if}
	</nav>

	<div class="flex-1 relative h-screen">
		{#key data.pathname}
			<div class="absolute left-3 top-1.5 right-0 bottom-0 overflow-y-auto" in:fade={{ duration: ANIMATE_DURATION, delay: ANIMATE_DELAY }} out:fade={{ duration: ANIMATE_DURATION }}>
				<slot />
			</div>
		{/key}
	</div>
</div>
