<template>
  <q-page class="justify-evenly">
    <div v-if="hasActiveConnections" class="q-pa-md">
      <!-- style="max-width: 400px" -->
      <div class="q-pa-md">
        <q-form ref="uploadForm" @submit="processUpload" class="q-gutter-md">
          <q-select
            filled
            v-model="bankConnection"
            :options="activeBankConnections"
            :option-label="bankConnectionLabel"
            label="EBICS Bank connection"
            hint="Select EBICS bank connection"
            lazy-rules
            :rules="[
              (val) =>
                bankConnection || 'Please select valid EBICS bank connection',
            ]"
          />

          <div v-if="bankConnection" class="q-gutter-sm">
            <!-- q-radio
              v-model="bankConnection.ebicsVersion"
              val="H003"
              label="EBICS 2.4 (H003)"
            /-->
            <q-radio
              v-model="bankConnection.ebicsVersion"
              :disable="!isEbicsVersionAllowedForUse(bankConnection.partner.bank, EbicsVersion.H004)"
              val="H004"
              label="EBICS 2.5 (H004)"
            />
            <q-radio
              v-model="bankConnection.ebicsVersion"
              :disable="!isEbicsVersionAllowedForUse(bankConnection.partner.bank, EbicsVersion.H005)"
              val="H005"
              label="EBICS 3.0 (H005)"
            />
          </div>

          <q-select
            v-if="
              bankConnection?.ebicsVersion == 'H003' ||
              bankConnection?.ebicsVersion == 'H004'
            "
            filled
            v-model="orderType"
            :options="orderTypes"
            :option-label="(t) => orderTypeLabel(t)"
            label="EBICS Order Type"
            hint="Select EBICS Order Type"
            lazy-rules
            :rules="[(val) => val || 'Please select valid EBICS Order Type']"
          >
            <template v-slot:option="scope">
              <q-item v-bind="scope.itemProps">
                <q-item-section>
                  <q-item-label v-html="orderTypeLabel(scope.opt)" />
                  <q-item-label caption>{{
                    scope.opt.description
                  }}</q-item-label>
                </q-item-section>
              </q-item>
            </template>
          </q-select>

          <q-select
            v-if="bankConnection?.ebicsVersion == 'H005'"
            filled
            v-model="btfType"
            :options="btfTypes"
            :option-label="(t) => btfTypeLabel(t)"
            label="Business Transaction Format"
            hint="Select EBICS Business Transaction Format (BTF)"
            lazy-rules
            :rules="[
              (val) => val || 'Please select valid EBICS BTF',
            ]"
          >
            <template v-slot:option="scope">
              <q-item v-bind="scope.itemProps">
                <q-item-section>
                  <q-item-label v-html="btfTypeLabel(scope.opt)" />
                  <q-item-label caption>{{
                    scope.opt.description
                  }}</q-item-label>
                </q-item-section>
              </q-item>
            </template>
          </q-select>

          <!-- DZHNN / OZHNN -->
          <q-toggle
            v-if="
              bankConnection?.ebicsVersion == 'H003' ||
              bankConnection?.ebicsVersion == 'H004'
            "
            v-model="signatureOZHNN"
            :label="
              signatureOZHNN ? 'Signature (OZHNN)' : 'No Signature (DZHNN)'
            "
          />
          <!-- signature flag, request EDS -->

          <q-toggle
            v-if="bankConnection?.ebicsVersion == 'H005'"
            v-model="signatureFlag"
            label="Signature flag"
          />
          <q-toggle
            v-if="bankConnection?.ebicsVersion == 'H005' && signatureFlag"
            v-model="requestEDS"
            label="Request EDS"
          />

          <q-btn-dropdown
            v-if="!fileEditor && userSettings.uploadOnDrop"
            :split="fileEditor"
            color="primary"
            label="Smart Adjustment Settings"
          >
            <user-preferences section-filter="ContentOptions" />
          </q-btn-dropdown>

          <q-file
            v-if="fileEditor"
            style="max-width: 300px"
            v-model="file"
            outlined
            label="Drop file here"
            hint="Max file size (20MB)"
            max-file-size="21000000"
            @rejected="onRejectedMessage(false)"
            @update:model-value="onUpdateInputFile"
          >
            <template v-slot:prepend>
              <q-icon name="attach_file" />
            </template>
          </q-file>

          <q-file
            v-else
            style="max-width: 300px"
            v-model="files"
            outlined
            multiple
            use-chips
            :label="
              'Drop file(s) here' +
              (userSettings.uploadOnDrop ? ' to upload' : '')
            "
            hint="Max file size (1GB)"
            max-file-size="1200000000"
            @rejected="onRejectedMessage(true)"
            @update:model-value="onUpdateInputFiles"
            lazy-rules
            :rules="[
              (val) =>
                val.length > 0 || 'Please drop or select file(s) for upload',
            ]"
          >
            <template v-slot:prepend>
              <q-icon name="attach_file" />
            </template>
          </q-file>

          <v-ace-editor
            ref="contentEditor"
            v-if="fileEditor && fileFormat != FileFormat.BINARY"
            v-model:value="fileText"
            :lang="editorLang"
            theme="clouds"
            style="height: 300px"
            :printMargin="false"
          />

          <!--
          <div>Format: {{fileFormat}}</div>
          <div>EditorLang: {{editorLang}}</div>
          -->

          <q-input
            v-if="fileEditor && bankConnection?.ebicsVersion == 'H005'"
            filled
            v-model="fileName"
            label="Uploaded filename"
            hint="For support purposes only"
            lazy-rules
            :rules="[
              (val) => val.length > 0 || 'Please provide input file name',
            ]"
          />

          <div class="q-pa-md q-gutter-sm">
            <q-btn-dropdown
              v-if="fileEditor || !userSettings.uploadOnDrop"
              :split="fileEditor"
              color="primary"
              label="Smart Adjust"
              @click="applySmartAdjustmentsForSingleFile()"
            >
              <user-preferences
                :section-filter="
                  fileEditor ? contentOptionsFilter : 'ContentOptions'
                "
              />
            </q-btn-dropdown>

            <q-btn
              v-if="fileEditor || !userSettings.uploadOnDrop"
              label="Upload"
              type="submit"
              color="primary"
            />
          </div>
        </q-form>
      </div>
    </div>
    <div v-else class="q-pa-md">
      <q-banner class="bg-grey-3">
        <template v-slot:avatar>
          <q-icon name="signal_wifi_off" color="primary" />
        </template>
        You have no initialized bank connection. Create and initialize one bank
        connection in order to upload files.
        <template v-slot:action>
          <q-btn
            flat
            color="primary"
            label="Manage bank connections"
            to="/bankconnections"
          />
        </template>
      </q-banner>
    </div>
  </q-page>
</template>

<script lang="ts">
import {
  BankConnection,
  UploadRequest,
  UploadRequestH004,
  UploadRequestH005,
  FileFormat,
  BTFType,
  OrderTypeFilter,
  OrderType,
  BankConnectionAccess,
  EbicsVersion,
} from 'components/models';
import { defineComponent } from 'vue';
import { ref, computed } from 'vue';

//Components
import { QForm, useQuasar } from 'quasar';
import { VAceEditor } from 'vue3-ace-editor';
import 'ace-builds/src-noconflict/mode-xml';
import 'ace-builds/src-noconflict/theme-clouds';
import UserPreferences from 'components/UserPreferences.vue';

//Composition APIs
import useBankConnectionsAPI from 'components/bankconnections';
import useFileTransferAPI from 'components/filetransfer';
import useTextUtils from 'components/text-utils';
import useUserSettings from 'components/user-settings';
import useOrderTypesAPI from 'components/order-types';
import useOrderTypeLabelAPI from 'components/order-type-label';
import useBanksAPI from 'src/components/banks';

export default defineComponent({
  name: 'FileUpload',
  components: { VAceEditor, UserPreferences },
  props: {
    fileEditor: {
      type: Boolean,
      required: true,
      default: false,
    },
  },
  setup(props) {
    //Selected bank connection
    const bankConnection = ref<BankConnection>();
    const replaceMsgId = ref(true);
    const { activeBankConnections, hasActiveConnections, bankConnectionLabel } =
      useBankConnectionsAPI(BankConnectionAccess.USE);
    const { ebicsUploadRequest } = useFileTransferAPI();
    const { applySmartAdjustments, detectFileFormat } = useTextUtils();
    const { isEbicsVersionAllowedForUse } = useBanksAPI(true);
    const { userSettings } = useUserSettings();
    const { orderTypeLabel, btfTypeLabel } = useOrderTypeLabelAPI();
    const { btfTypes, orderTypes } =
      useOrderTypesAPI(bankConnection, activeBankConnections, ref(OrderTypeFilter.UploadOnly));

    //Single file setup
    const testInput = ref(null);
    const file = ref<File>();
    const fileRawText = ref<string>(''); //Original text of input file
    const fileText = ref<string>('<document>paste document here</document>'); //Text displayed in editor (in case of no binary)
    const fileName = ref('');
    const orderType = ref<OrderType>();
    const btfType = ref<BTFType>();

    const signatureFlag = ref(true);
    const requestEDS = ref(true);
    const signatureOZHNN = ref(true);

    //Multiple file setup
    const files = ref<File[]>([]);
    const resetFiles = () => {
      files.value = [];
    };

    //Reference to upload Form because of validation
    const uploadForm = ref<QForm>()

    const q = useQuasar();

    const fileFormat = computed((): FileFormat => {
      return detectFileFormat(fileText.value);
    });

    const contentOptionsFilter = computed((): string => {
      if (fileFormat.value == FileFormat.XML) return 'ContentOptions.Pain.00x';
      else if (fileFormat.value == FileFormat.SWIFT)
        return 'ContentOptions.Swift';
      else return 'ContentOptions';
    });

    const editorLang = computed((): string => {
      console.log('File format detected: ' + fileFormat.value.toString());
      if (fileFormat.value == FileFormat.XML) return 'xml';
      if (fileFormat.value == FileFormat.SWIFT) return 'text';
      else return 'xml';
    });

    const getUploadRequest = (fileNameOverload?: string): UploadRequest => {
      if (bankConnection.value?.ebicsVersion == 'H005') {
        return {
          orderService: btfType.value?.service,
          signatureFlag: signatureFlag.value,
          edsFlag: requestEDS.value,
          fileName: fileNameOverload ? fileNameOverload : fileName.value,
        } as UploadRequestH005;
      } else {
        //H004, H003
        return {
          orderType: orderType.value?.orderType,
          attributeType: signatureOZHNN.value ? 'OZHNN' : 'DZHNN',
          params: new Map(),
        } as UploadRequestH004;
      }
    };

    const getUploadContent = (): Blob => {
      if (fileFormat.value == FileFormat.BINARY && file.value) {
        return file.value;
      } else if (fileFormat.value != FileFormat.BINARY && fileText.value) {
        return new Blob([fileText.value], { type: 'text/html' });
      } else {
        throw new Error(
          'Invalid input combination, no file content available to upload'
        );
      }
    };

    const processUpload = async (): Promise<void> => {
      if (bankConnection.value) {
        if (!props.fileEditor) {
          for (let file of files.value) {
            await ebicsUploadRequest(
              bankConnection.value,
              getUploadRequest(file.name),
              file
            );
          }
        } else {
          //Single file upload
          await ebicsUploadRequest(
            bankConnection.value,
            getUploadRequest(),
            getUploadContent()
          );
        }
      }
    };

    /**
     * Load text of the input file into text area
     * (only for text files, binary cant be edited)
     */
    const onUpdateInputFile = async (inputFile: File) => {
      file.value = inputFile;
      fileName.value = inputFile.name;
      try {
        fileRawText.value = await file.value.text();
        const detectedFileFormat = detectFileFormat(fileRawText.value);
        if (detectedFileFormat == FileFormat.BINARY) {
          file.value = undefined;
          q.notify({
            color: 'positive',
            position: 'bottom-right',
            message:
              "Binary file detected, please use 'Simple file upload' instead.",
            icon: 'warning',
          });
        } else {
          if (userSettings.value.adjustmentOptions.applyAutomatically) {
            fileText.value = await applySmartAdjustments(
              fileRawText.value,
              detectedFileFormat,
              userSettings.value
            );
          } else {
            fileText.value = fileRawText.value;
          } 
        }
      } catch (error) {
        q.notify({
          color: 'negative',
          position: 'bottom-right',
          message: `Loading file failed: ${JSON.stringify(error)}`,
          icon: 'report_problem',
        });
      }
    };

    const applySmartAdjustmentsForSingleFile =  async() => {
      fileText.value = await applySmartAdjustments(
        fileText.value,
        fileFormat.value,
        userSettings.value
      );
    };

    const onUpdateInputFiles = async(inputFiles: File[]) => {
      console.log(inputFiles);
      files.value = inputFiles;

      if (userSettings.value.uploadOnDrop) {
        const validationResult = await (
          uploadForm.value as QForm
        ).validate();
        if (!validationResult) {
          files.value = [];
          q.notify({
            color: 'warning',
            position: 'bottom-right',
            message: 'File will be not uploaded',
            caption:
              'Please correct validation issues before uploading the file',
            icon: 'warning',
          });
        }
      }
    };

    const onRejectedMessage = (multiple: boolean) => {
      q.notify({
        type: 'negative',
        message:
          `File must smaller than ${multiple ? '1.2GB' : '20MB'}, for bigger files use 'Simple file upload'`
      });
    };

    return {
      bankConnection,
      activeBankConnections,
      hasActiveConnections,
      bankConnectionLabel,
      isEbicsVersionAllowedForUse,
      EbicsVersion,

      userSettings,
      replaceMsgId,
      ebicsUploadRequest,
      applySmartAdjustments,
      detectFileFormat,
      contentOptionsFilter,

      //Multiple files
      files,
      resetFiles,
      onUpdateInputFiles,

      //Sigle file
      file,
      fileRawText,
      fileText,
      fileName,
      editorLang,
      fileFormat,
      onUpdateInputFile,
      applySmartAdjustmentsForSingleFile,
      onRejectedMessage,

      testInput,

      //Commons order types
      orderType,
      orderTypes,
      orderTypeLabel,

      //Commons BTF types
      btfType,
      btfTypes,
      btfTypeLabel,

      //Commons request flags
      signatureFlag,
      requestEDS,
      signatureOZHNN,
      FileFormat,
      processUpload,
    };
  },
});
</script>
