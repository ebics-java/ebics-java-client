import { AutoAdjustmentsPain00x } from './models';
/**
 * Text Utils composition API
 * @returns
 *  findAndReplace generic replacement function for aceEditor
 *  findAndReplaceMsgIds function replacing MsgIds
 */
export default function useTextUtils() {
  
  /**
   * Generic find & replace
   * @param input input string
   * @param regExp reg expression which is to be searched
   * @param replaceWith function
   * @returns the @param input with all replacements applied
   */
  const findAndReplace = (
    input: string,
    regExp: RegExp,
    replaceWith: (match: string) => string
  ): Promise<string> => {
    return new Promise<string>((resolve) => {
      resolve(input.replace(regExp, replaceWith));
    });
  };

  /**
   * 
   * @returns current date in YYYY-MM-DD format
   */
  const currentDate = () => {
    const date = new Date();
    return `${date.getFullYear()}-${date
      .getUTCMonth()
      .toString()
      .padStart(2, '0')}-${date.getUTCDate().toString().padStart(2, '0')}`;
  };

  /*const applySmartAdjustmentsSwift = async (
    fileText: string,
    settings: AutoAdjustmentsSwift,
  ): Promise<string> => {
    return '';
  }*/

  const applySmartAdjustmentsPain00x = async (
    fileText: string,
    settings: AutoAdjustmentsPain00x,
  ): Promise<string> => {
    const s = settings;
    const idPrefix = s.idPrefix + '-' + new Date().toDateString() + new Date().toTimeString();
    let bLevel = 0;
    let cLevel = 0;
    let nbOfTrxs = 0;
    let ctrlSum = 0;
    const regExp = new RegExp(
      (s.pmtInfId || s.instrId || s.endToEndId ? '(<PmtInfId>.*</PmtInfId>)' : '') +
        (s.instrId ? '|(<InstrId>.*</InstrId>)' : '') +
        (s.endToEndId || s.nbOfTrxsCalc ? '|(<EndToEndId>.*</EndToEndId>)' : '') +
        (s.ctrlSumCalc
          ? '|(<InstdAmt Ccy="\\w{3}">.*<\\/InstdAmt>)|(<Amt Ccy="\\w{3}">.*<\\/Amt>)'
          : '') +
        (s.creDtTm ? '|(<CreDtTm>.*<\\/CreDtTm>)' : '') +
        (s.reqdExctnDt ? '|(<ReqdExctnDt>.*<\\/ReqdExctnDt>)' : '') +
        (s.msgId ? '|(<MsgId>.*</MsgId>)' : ''),
      'g'
    );
    console.log('Source regexp: ' + regExp.source);
    const output = await findAndReplace(fileText, regExp, (match) => {
      if (s.msgId && match.startsWith('<MsgId>')) {
        return `<MsgId>${idPrefix}</MsgId>`;
      } else if (match.startsWith('<PmtInfId>')) {
        cLevel = 0;
        bLevel++;
        return s.pmtInfId ? `<PmtInfId>${idPrefix}-B${bLevel}</PmtInfId>` : match;
      } else if (s.instrId && match.startsWith('<InstrId>')) {
        return `<InstrId>${idPrefix}-B${bLevel}-C${cLevel + 1}</InstrId>`;
      } else if (
        (s.endToEndId || s.nbOfTrxsCalc) &&
        match.startsWith('<EndToEndId>')
      ) {
        cLevel++;
        nbOfTrxs++;
        return s.endToEndId
          ? `<EndToEndId>${idPrefix}-B${bLevel}-C${cLevel}</EndToEndId>`
          : match;
      } else if (
        s.ctrlSumCalc &&
        (match.startsWith('<InstdAmt') || match.startsWith('<Amt'))
      ) {
        const amt = Number(
          match.substring(match.indexOf('>') + 1, match.lastIndexOf('<'))
        );
        ctrlSum += amt;
        return match;
      } else if (s.reqdExctnDt && match.startsWith('<ReqdExctnDt>')) {
        return `<ReqdExctnDt>${currentDate()}</ReqdExctnDt>`;
      } else if (s.creDtTm && match.startsWith('<CreDtTm>')) {
        return `<CreDtTm>${new Date().toISOString()}</CreDtTm>`;
      } else return 'Unknown match';
    });
    if (s.nbOfTrxsCalc || s.ctrlSumCalc) {
      return await findAndReplace(output,
        new RegExp(
          (s.nbOfTrxsCalc ? '(<NbOfTxs>.*</NbOfTxs>)' : '') +
            (s.nbOfTrxsCalc && s.ctrlSumCalc ? '|' : '') +
            (s.ctrlSumCalc ? '(<CtrlSum>.*</CtrlSum>)' : ''),
          'g'
        ),
        (match) => {
          if (s.nbOfTrxsCalc && match.startsWith('<NbOfTxs>')) {
            return `<NbOfTxs>${nbOfTrxs}</NbOfTxs>`;
          } else if (s.ctrlSumCalc && match.startsWith('<CtrlSum>')) {
            return `<CtrlSum>${ctrlSum.toFixed(5).replace(new RegExp('(\\.)?0+$'), '')}</CtrlSum>`;
          } else return 'Unknown match';
        }
      );
    } else {
      return output;
    }
  };

  return { currentDate, applySmartAdjustmentsPain00x };
}
