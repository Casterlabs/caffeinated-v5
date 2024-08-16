<script lang="ts">
	import type { Writable } from 'svelte/store';
	import type { KoiAccount } from '../app';
	import { goto } from '$app/navigation';
	import { svelte } from '$lib/app';

	const accounts: Writable<KoiAccount[]> = svelte('App.koi', 'accounts');
	let manualBypass = false;

	setTimeout(() => (manualBypass = true), 15 * 1000);

	accounts.subscribe((accounts) => {
		if (!accounts) return;

		const accountArray = Object.values(accounts);

		if (accountArray.length == 0) {
			// TODO goto("/login");
			return;
		}

		for (const account of accountArray) {
			if (account.isAlive) {
				goto('/dashboard');
			}
		}
	});
</script>

{#if manualBypass}
	<a class="text-link" href="/dashboard">manual bypass</a>
{/if}
Loading...
