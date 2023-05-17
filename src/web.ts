import { WebPlugin } from '@capacitor/core';

import type { TLSSenderPlugin } from './definitions';

export class TLSSenderWeb extends WebPlugin implements TLSSenderPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
