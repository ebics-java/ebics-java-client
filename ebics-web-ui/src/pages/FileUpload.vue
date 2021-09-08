<template>
  <q-page class="justify-evenly">
    <div v-if="hasActiveConnections" class="q-pa-md">
      <h5 v-if="fileEditor">Edit &amp; upload File</h5>
      <h5 v-else>Simple file upload</h5>

      <!-- style="max-width: 400px" -->
      <div class="q-pa-md">
        <q-form ref="uploadForm" @submit="onSubmit" class="q-gutter-md">
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
              val="H004"
              label="EBICS 2.5 (H004)"
            />
            <q-radio
              v-model="bankConnection.ebicsVersion"
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
            label="BTF Message Type"
            hint="Select EBICS BTF Message Type"
            lazy-rules
            :rules="[
              (val) => val || 'Please select valid EBICS BTF Message Type',
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
            @rejected="onRejected"
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
            @rejected="onRejected"
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
  User,
  UploadRequest,
  UploadRequestH004,
  UploadRequestH005,
  FileFormat,
  BTFType,
  OrderTypeFilter,
  OrderType,
} from 'components/models';
import { defineComponent } from 'vue';
import { ref, computed } from 'vue';

//Components
import { QForm } from 'quasar';
import { VAceEditor } from 'vue3-ace-editor';
import 'ace-builds/src-noconflict/mode-xml';
import 'ace-builds/src-noconflict/theme-clouds';
import { VAceEditorInstance } from 'vue3-ace-editor/types';
import UserPreferences from 'components/UserPreferences.vue';

//Composition APIs
import useBankConnectionsAPI from 'components/bankconnections';
import useFileTransferAPI from 'components/filetransfer';
import useTextUtils from 'components/text-utils';
import useUserSettings from 'components/user-settings';
import useOrderTypesAPI from 'components/order-types';

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
  data() {
    return {};
  },
  methods: {
    async applySmartAdjustmentsForSingleFile() {
      this.fileText = await this.applySmartAdjustments(
        this.fileText,
        this.fileFormat,
        this.userSettings
      );
    },

    async onUpdateInputFiles(files: File[]) {
      console.log(files);
      this.files = files;

      if (this.userSettings.uploadOnDrop) {
        const validationResult = await (
          this.$refs.uploadForm as QForm
        ).validate();
        if (!validationResult) {
          this.files = [];
          this.$q.notify({
            color: 'warning',
            position: 'bottom-right',
            message: 'File will be not uploaded',
            caption:
              'Please correct validation issues before uploading the file',
            icon: 'warning',
          });
        }
      }
    },

    /**
     * Load text of the input file into text area
     * (only for text files, binary cant be edited)
     */
    async onUpdateInputFile(file: File) {
      console.log(file.name);
      this.file = file;
      this.fileName = file.name;
      try {
        this.fileRawText = await file.text();
        if (this.detectFileFormat(this.fileRawText) == FileFormat.BINARY) {
          this.$q.notify({
            color: 'positive',
            position: 'bottom-right',
            message:
              "Binary files can't be edited, but you can still upload them",
            icon: 'warning',
          });
        } else {
          if (this.userSettings.adjustmentOptions.applyAutomatically)
            this.fileText = await this.applySmartAdjustments(
              this.fileRawText,
              this.fileFormat,
              this.userSettings
            );
          else this.fileText = this.fileRawText;
        }
      } catch (error) {
        this.$q.notify({
          color: 'negative',
          position: 'bottom-right',
          message: `Loading file failed: ${JSON.stringify(error)}`,
          icon: 'report_problem',
        });
      }
    },


    async onSubmit() {
      await this.processUpload();
    },

    onRejected() {
      //rejectedFiles: File[]
      this.$q.notify({
        type: 'negative',
        message:
          'File must smaller than 20MB, for bigger files use upload without editor',
      });
    },
  },
  setup(props) {
    //Selected bank connection
    const bankConnection = ref<User>();
    const replaceMsgId = ref(true);
    const { activeBankConnections, hasActiveConnections, bankConnectionLabel } =
      useBankConnectionsAPI();
    const { ebicsUploadRequest } = useFileTransferAPI();
    const { applySmartAdjustments, detectFileFormat } = useTextUtils();
    const { userSettings } = useUserSettings();
    const { btfTypes, orderTypes, orderTypeLabel, btfTypeLabel } =
      useOrderTypesAPI(bankConnection, ref(OrderTypeFilter.UploadOnly));

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

    return {
      bankConnection,
      activeBankConnections,
      hasActiveConnections,
      bankConnectionLabel,

      userSettings,
      replaceMsgId,
      ebicsUploadRequest,
      applySmartAdjustments,
      detectFileFormat,
      contentOptionsFilter,

      //Multiple files
      files,
      resetFiles,

      //Sigle file
      file,
      fileRawText,
      fileText,
      fileName,
      editorLang,
      fileFormat,

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
