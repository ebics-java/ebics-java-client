import { describe, expect, it, beforeAll } from '@jest/globals';
import { installQuasarPlugin } from '@quasar/quasar-app-extension-testing-unit-jest';
import { FileFormat, UserSettings, AutoAdjustmentsPain00x, AutoAdjustmentsSwift } from 'src/components/models';
import useTextUtils from 'src/components/text-utils';

// Specify here Quasar config you'll need to test your component
installQuasarPlugin();

describe('TextUtils', () => {

  let applySmartAdjustmentsFn: (fileText: string, fileFormat: FileFormat, settings: UserSettings)=>Promise<string>;
  const defaultSettings = {
    uploadOnDrop: true,
    testerSettings: true,
    adjustmentOptions: {
      applyAutomatically: true,
      pain00x: {
        msgId: true,
        pmtInfId: true,
        instrId: true,
        endToEndId: true,
        uetr: true,
        reqdExctnDt: true,
        creDtTm: true,
        nbOfTrxsCalc: true,
        ctrlSumCalc: true,
        idPrefix: 'testpref'
      } as AutoAdjustmentsPain00x,
      swift: {
        uetr: true,
        f20: true,
        f21: true,
        f30: true,
        idPrefix: 'testpref',
        randomIds: false,
      } as AutoAdjustmentsSwift,
    }
  } as UserSettings

  beforeAll(() => {
    const { applySmartAdjustments } = useTextUtils();
    applySmartAdjustmentsFn = applySmartAdjustments;
  });

  it('is empty input adjusted correctly', async() => {
    expect(await applySmartAdjustmentsFn('', FileFormat.TEXT, defaultSettings)).toBe('');
  });

  it('is UETR input in MT101 adjusted correctly', async() => {
    const uetr = await applySmartAdjustmentsFn('{121:uetr}', FileFormat.TEXT, defaultSettings)
    console.log(uetr);
    const expectedLength = '{121:}'.length + 36
    expect(uetr.length).toBe(expectedLength);
    expect(uetr.startsWith('{121:')).toBe(true);
    expect(uetr.endsWith('}')).toBe(true);
  });

  it('is UETR input in MT101 with tripple brackets adjusted correctly', async() => {
    const uetr = await applySmartAdjustmentsFn('{{{121:uetr}}}', FileFormat.TEXT, defaultSettings)
    console.log(uetr);
    const expectedLength = '{{{121:}}}'.length + 36
    expect(uetr.length).toBe(expectedLength);
    expect(uetr.startsWith('{{{121:')).toBe(true);
    expect(uetr.endsWith('}}}')).toBe(true);
  });

  it('is non UETR input in Pain.001 adjusted correctly', async() => {
    const uetr = await applySmartAdjustmentsFn('<InstrInf>non-uetr</InstrInf>', FileFormat.XML, defaultSettings)
    console.log(uetr);
    expect(uetr).toBe('<InstrInf>non-uetr</InstrInf>');
  });

  it('is UETR input in Pain.001.001.03 adjusted correctly', async() => {
    const uetr = await applySmartAdjustmentsFn('<InstrInf>UETR/</InstrInf>', FileFormat.XML, defaultSettings)
    console.log(uetr);
    const expectedLength = '<InstrInf>UETR/</InstrInf>'.length + 36
    expect(uetr.length).toBe(expectedLength);
    expect(uetr.startsWith('<InstrInf>UETR/')).toBe(true);
    expect(uetr.endsWith('</InstrInf>')).toBe(true);
  });

  it('is UETR input in Pain.001.001.09 adjusted correctly', async() => {
    const uetr = await applySmartAdjustmentsFn('<UETR>xxx</UETR>', FileFormat.XML, defaultSettings)
    console.log(uetr);
    const expectedLength = '<UETR></UETR>'.length + 36
    expect(uetr.length).toBe(expectedLength);
    expect(uetr.startsWith('<UETR>')).toBe(true);
    expect(uetr.endsWith('</UETR>')).toBe(true);
  });
});
