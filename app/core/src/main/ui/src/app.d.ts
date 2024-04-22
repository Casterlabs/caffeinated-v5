import type { Writable } from "svelte/store";

declare type Stores = {
	readonly svelte(propertyName: string): Writable<null | any>
}

declare type Preferences  = {
	readonly __stores: Stores;

	readonly ui: {
		readonly __stores: Stores;	
		theme: Promise<string> | string;
		zoom: Promise<number> | number;
		width: Promise<number> | number;
		height: Promise<number> | number;
	} ;

	readonly async save(): void
};

declare type Themes = {
	readonly map: {
		[key: string]: {
			readonly isDark: boolean;
			readonly baseScale: string[12];
			readonly accentScale: string[12];
			readonly baseScaleP3: null | string[12];
			readonly accentScaleP3: null | string[12];
		};
	};
};

declare type App = {
	readonly __stores: Stores;

	readonly preferences: Preferences;
	readonly themes: Promise<Themes>;
};

declare global {
	interface Window {
		App: App;
	}
}

export {};
