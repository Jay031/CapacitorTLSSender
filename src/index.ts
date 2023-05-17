import { registerPlugin } from '@capacitor/core';

import type { TLSSenderPlugin } from './definitions';

const TLSSender = registerPlugin<TLSSenderPlugin>('TLSSender', {
  web: () => import('./web').then(m => new m.TLSSenderWeb()),
});

export * from './definitions';
export { TLSSender };
