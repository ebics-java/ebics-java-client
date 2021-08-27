import { Ref } from 'vue';
import { VAceEditorInstance } from 'vue3-ace-editor/types';
import { Ace } from 'ace-builds';
import { Range } from 'ace-builds';
/**
 * Text Utils composition API
 * @returns
 *  findAndReplace generic replacement function for aceEditor
 *  findAndReplaceMsgIds function replacing MsgIds
 */
export default function useTextUtils(text: Ref<string>) {
  const findAndReplace = (
    regExp: RegExp,
    replaceWith: (match: string) => string
  ): Promise<number> => {
    return new Promise<number>((resolve) => {
      let occurences = 0;
      const replaceWithIndex = (match: string): string => {
        occurences++;
        return replaceWith(match);
      };

      text.value = text.value.replace(regExp, replaceWithIndex);
      resolve(occurences);
    });
  };

  const findAndReplaceEditor = (
    editorInstance: VAceEditorInstance,
    regExp: RegExp,
    replaceWith: (index: number) => string
  ): Promise<number> => {
    return new Promise<number>((resolve, reject) => {
      const editor = editorInstance._editor;
      if (!editor) reject('Editor is not mouted');
      else {
        const needle: string = regExp as unknown as string;
        let index = 0;
        editor.find(needle, {
          start: new Range(0, 0, 0, 0),
          range: new Range(0, 0, editor.session.getLength(), 100),
        });

        //Until we have found something
        while (!editor.selection.isEmpty()) {
          //Let's replace and increase index
          editor.replace(replaceWith(index++));

          //We must remove selection and move to next
          //otherwise the same occurence would be found
          editor.selection.clearSelection();
          editor.selection.moveCursorRight();
          console.log(
            `Current selection: ${JSON.stringify(editor.selection.getRange())}`
          );

          const nextRange = Range.fromPoints(editor.selection.getRange().end, {
            row: editor.session.getLength(),
            column: 100,
          } as Ace.Point);

          const startRange = Range.fromPoints(
            editor.selection.getRange().end,
            editor.selection.getRange().end
          );
          console.log(`Next range: ${JSON.stringify(nextRange)}`);
          console.log(`Start range: ${JSON.stringify(startRange)}`);
          //Lets find the next occurence
          editor.findNext({ start: startRange, range: nextRange });
          console.log(
            'Search completed, found: ' +
              (!editor.selection.isEmpty() ? 'true' : 'false')
          );
        }
        resolve(index);
      }
    });
  };

  const findAndReplaceMsgIdsEditor = async (
    editorInstance: VAceEditorInstance
  ): Promise<number> => {
    return await findAndReplaceEditor(
      editorInstance,
      new RegExp('<PmtInfId>.*</PmtInfId>'),
      (index) => {
        return `<PmtInfId>${index}</PmtInfId>`;
      }
    );
  };

  const currentDate = () => {
    const date = new Date();
    return `${date.getFullYear()}-${date
      .getUTCMonth()
      .toString()
      .padStart(2, '0')}-${date.getUTCDate().toString().padStart(2, '0')}`;
  };

  const findAndReplaceMsgIds = async (
    msgId: boolean,
    pmtInfId: boolean,
    instrId: boolean,
    endToEndId: boolean,
    reqdExctnDt: boolean,
    creDtTm: boolean,
    nbOfTrxsCalc: boolean,
    ctrlSumCals: boolean,
    idPrefix: string
  ): Promise<number> => {
    let bLevel = 0;
    let cLevel = 0;
    let nbOfTrxs = 0;
    let ctrlSum = 0;
    const regExp = new RegExp(
      (pmtInfId || instrId || endToEndId ? '(<PmtInfId>.*</PmtInfId>)' : '') +
        (instrId ? '|(<InstrId>.*</InstrId>)' : '') +
        (endToEndId || nbOfTrxsCalc ? '|(<EndToEndId>.*</EndToEndId>)' : '') +
        (ctrlSumCals
          ? '|(<InstdAmt Ccy="\\w{3}">.*<\\/InstdAmt>)|(<Amt Ccy="\\w{3}">.*<\\/Amt>)'
          : '') +
        (creDtTm ? '|(<CreDtTm>.*<\\/CreDtTm>)' : '') +
        (reqdExctnDt ? '|(<ReqdExctnDt>.*<\\/ReqdExctnDt>)' : '') +
        (msgId ? '|(<MsgId>.*</MsgId>)' : ''),
      'g'
    );
    console.log('Source regexp: ' + regExp.source);
    let occurences = await findAndReplace(regExp, (match) => {
      if (msgId && match.startsWith('<MsgId>')) {
        return `<MsgId>${idPrefix}</MsgId>`;
      } else if (match.startsWith('<PmtInfId>')) {
        cLevel = 0;
        bLevel++;
        return pmtInfId ? `<PmtInfId>${idPrefix}-B${bLevel}</PmtInfId>` : match;
      } else if (instrId && match.startsWith('<InstrId>')) {
        return `<InstrId>${idPrefix}-B${bLevel}-C${cLevel + 1}</InstrId>`;
      } else if (
        (endToEndId || nbOfTrxsCalc) &&
        match.startsWith('<EndToEndId>')
      ) {
        cLevel++;
        nbOfTrxs++;
        return endToEndId
          ? `<EndToEndId>${idPrefix}-B${bLevel}-C${cLevel}</EndToEndId>`
          : match;
      } else if (
        ctrlSumCals &&
        (match.startsWith('<InstdAmt') || match.startsWith('<Amt'))
      ) {
        const amt = Number(
          match.substring(match.indexOf('>') + 1, match.lastIndexOf('<'))
        );
        ctrlSum += amt;
        return match;
      } else if (reqdExctnDt && match.startsWith('<ReqdExctnDt>')) {
        return `<ReqdExctnDt>${currentDate()}</ReqdExctnDt>`;
      } else if (creDtTm && match.startsWith('<CreDtTm>')) {
        return `<CreDtTm>${new Date().toISOString()}</CreDtTm>`;
      } else return 'Unknown match';
    });
    if (nbOfTrxsCalc || ctrlSumCals) {
      occurences += await findAndReplace(
        new RegExp(
          (nbOfTrxsCalc ? '(<NbOfTxs>.*</NbOfTxs>)' : '') +
            (nbOfTrxsCalc && ctrlSumCals ? '|' : '') +
            (ctrlSumCals ? '(<CtrlSum>.*</CtrlSum>)' : ''),
          'g'
        ),
        (match) => {
          if (nbOfTrxsCalc && match.startsWith('<NbOfTxs>')) {
            return `<NbOfTxs>${nbOfTrxs}</NbOfTxs>`;
          } else if (ctrlSumCals && match.startsWith('<CtrlSum>')) {
            return `<CtrlSum>${ctrlSum}</CtrlSum>`;
          } else return 'Unknown match';
        }
      );
    }
    return occurences;
  };

  return { findAndReplaceMsgIdsEditor, findAndReplaceMsgIds };
}
