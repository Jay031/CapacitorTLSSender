export interface TLSSenderPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
