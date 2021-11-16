import { describe, expect, it } from '@jest/globals';
import { installQuasarPlugin } from '@quasar/quasar-app-extension-testing-unit-jest';
import useBanksAPI from 'src/components/banks'
import { Bank, EbicsVersion, EbicsVersionSettings } from 'src/components/models';

// Specify here Quasar config you'll need to test your component
installQuasarPlugin();

describe('Banks API', () => {
  it('is EBICS version allowed for use', () => {
    const { isEbicsVersionAllowedForUse } = useBanksAPI();
    const bank = { 
      ebicsVersions: [
        {
          version: EbicsVersion.H004,
          isAllowedForUse: true
        } as EbicsVersionSettings,
        {
          version: EbicsVersion.H005,
          isAllowedForUse: false
        } as EbicsVersionSettings,
      ]
    } as Bank;

    expect(isEbicsVersionAllowedForUse(bank, EbicsVersion.H004)).toBe(true);
    expect(isEbicsVersionAllowedForUse(bank, EbicsVersion.H005)).toBe(false);
    expect(isEbicsVersionAllowedForUse(bank, EbicsVersion.H006)).toBe(false);
  });

});
