import { WebPlugin } from '@capacitor/core';
import { FujitsuThermalPrinterPlugin } from './definitions';

export class FujitsuThermalPrinterWeb extends WebPlugin implements FujitsuThermalPrinterPlugin {
  constructor() {
    super({
      name: 'FujitsuThermalPrinter',
      platforms: ['web']
    });
  }

  async echo(options: { value: string }): Promise<{value: string}> {
    console.log('ECHO', options);
    return options;
  }
}

const FujitsuThermalPrinter = new FujitsuThermalPrinterWeb();

export { FujitsuThermalPrinter };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(FujitsuThermalPrinter);
