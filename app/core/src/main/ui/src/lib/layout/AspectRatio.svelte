<script lang="ts">
	export let aspectRatio = '16:9';
	$: [w, h] = aspectRatio.split(':').map(Number);
</script>

<div class="aspect-container" style:--aspect={h / w}>
	<div>
		<slot />
	</div>
</div>

<style>
	.aspect-container {
		position: relative;
		padding-bottom: calc(var(--aspect) * 100%);
		overflow: hidden;
	}
	.aspect-container > div {
		position: absolute;
		top: 0;
		left: 0;
		height: 100%;
		width: 100%;
	}
</style>
