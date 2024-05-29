import type { Writable } from "svelte/store";

export declare type Stores = {
	readonly svelte(propertyName: string): Writable<null | any>
}

export declare type Preferences  = {
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

export declare type Theme = {
	readonly name: string;
	readonly wordmarkDataUri: string;
	readonly isDark: boolean;
	readonly baseScale: string[12];
	readonly accentScale: string[12];
	readonly baseScaleP3: null | string[12];
	readonly accentScaleP3: null | string[12];
};

export declare type Themes = {
	readonly map: {
		[key: string]: Theme;
	};
};

export declare type KoiAccount  = {
	readonly isAlive: boolean;
	readonly token: String;
	readonly profile: any; // TODO types for events...
};

export declare type Koi  = {
	readonly __stores: Stores;

	readonly accounts: Promise<{[key: string]: KoiAccount}> | {[key: string]: KoiAccount};	
	readonly history: Promise<any[]>  | any[]; // TODO types for events...
};

export declare type App = {
	readonly __stores: Stores;

	readonly preferences: Preferences;
	readonly koi: Koi;
	readonly themes: Promise<Themes>;
};

declare global {
	interface Window {
		App: App;
	}
}

export {};
