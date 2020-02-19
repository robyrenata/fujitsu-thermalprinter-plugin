declare module "@capacitor/core" {
  interface PluginRegistry {
    FujitsuThermalPrinter: FujitsuThermalPrinterPlugin;
  }
}

export interface FujitsuThermalPrinterPlugin {
  echo(options: { value: string }): Promise<{value: string}>;
}
