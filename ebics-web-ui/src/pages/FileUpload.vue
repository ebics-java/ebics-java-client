<template>
  <q-page class="justify-evenly" ref="testInput">
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
            :option-label="userLabel"
            label="EBICS Bank connection"
            hint="Select EBICS bank connection"
            lazy-rules
            :rules="[
              (val) =>
                bankConnection || 'Please select valid EBICS bank connection',
            ]"
          />

          <div v-if="bankConnection" class="q-gutter-sm">
            <q-radio
              v-model="bankConnection.ebicsVersion"
              val="H003"
              label="EBICS 2.4 (H003)"
            />
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
            label="EBICS Order Type"
            hint="Select EBICS Order Type"
            lazy-rules
            :rules="[
              (val) =>
                (val && val.length > 0) ||
                'Please select valid EBICS Order Type',
            ]"
          />

          <q-select
            v-if="bankConnection?.ebicsVersion == 'H005'"
            filled
            v-model="btfType"
            :options="btfTypes"
            :option-label="(t) => btfLabel(t)"
            label="BTF Message Type"
            hint="Select EBICS BTF Message Type"
            lazy-rules
            :rules="[
              (val) => val || 'Please select valid EBICS BTF Message Type',
            ]"
          />

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
            lang="xml"
            theme="clouds"
            style="height: 300px"
            :printMargin="false"
          />
          <!-- @init="initEditor" -->

          <q-input
            
            v-if="bankConnection?.ebicsVersion == 'H005'"
            filled
            v-model="fileName"
            label="Uploaded filename"
            hint="For support purposes only"
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
  Btf,
  BtfMessage,
  UploadRequest,
  UploadRequestH004,
  UploadRequestH005,
  FileFormat,
} from 'components/models';
import { defineComponent } from 'vue';
import { ref, computed, watch, onMounted } from 'vue';
import { QForm } from 'quasar';

import { VAceEditor } from 'vue3-ace-editor';
import 'ace-builds/src-noconflict/mode-xml';
import 'ace-builds/src-noconflict/theme-clouds';
import { VAceEditorInstance } from 'vue3-ace-editor/types';

import useBankConnectionsAPI from 'components/bankconnections';
import useFileTransferAPI from 'components/filetransfer';
import useTextUtils from 'components/text-utils';
import useUserSettings from 'components/user-settings';
import UserPreferences from 'components/UserPreferences.vue';

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
    initEditor(editor: VAceEditorInstance) {
      console.log(`Initialize ace editor: ${JSON.stringify(editor.$options)}`);
    },
    async applySmartAdjustmentsForSingleFile() {
      this.fileText = await this.applySmartAdjustments(
        this.fileText,
        this.fileFormat,
        this.userSettings
      );
    },

    btfLabel(btf: Btf | undefined): string {
      return btf instanceof Btf ? btf.label() : '';
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
        if (this.fileFormat == FileFormat.BINARY) {
          this.$q.notify({
            color: 'positive',
            position: 'bottom-right',
            message:
              "Binary files can't be edited, although you can still upload the file",
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

    /**
     * Display label of the bankConnection
     */
    userLabel(bankConnection: User | undefined): string {
      if (
        bankConnection &&
        bankConnection.userId.trim().length > 0 &&
        bankConnection.name.trim().length > 0
      ) {
        return `${bankConnection.userId} | ${bankConnection.name}`;
      } else {
        return '';
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
    const replaceMsgId = ref(true);
    const { activeBankConnections, hasActiveConnections } =
      useBankConnectionsAPI();
    const { ebicsUploadRequest } = useFileTransferAPI();
    const { applySmartAdjustments, detectFileFormat } = useTextUtils();
    const { userSettings } = useUserSettings();

    //Selected bank connection
    const bankConnection = ref<User>();

    //Single file setup
    const contentEditor = ref<VAceEditorInstance | null>(null);
    const testInput = ref(null);
    const file = ref<File>();
    const fileRawText = ref<string>(''); //Original text of input file
    const fileText = ref<string>('<document>paste document here</document>'); //Text displayed in editor (in case of no binary)
    const fileName = ref('');
    const orderType = ref('');
    const orderTypes = ref(['XE2', 'XE3', 'XL3', 'XG1', 'CCT']);
    const btfType = ref<Btf>();
    const btfTypes = ref<Btf[]>([
      new Btf('PSR', undefined, 'CH', 'ZIP', new BtfMessage('pain.002')),
      new Btf('MCT', undefined, 'CH', undefined, new BtfMessage('pain.001')),
      new Btf('MCT', 'XCH', 'CGI', undefined, new BtfMessage('pain.001')),
    ]);
    const signatureFlag = ref(true);
    const requestEDS = ref(true);
    const signatureOZHNN = ref(true);
    onMounted(() => {
      console.log('Editor ref: ' + JSON.stringify(contentEditor.value));
      console.log('Test input ref: ' + JSON.stringify(testInput.value));
    });

    //Multiple file setup
    const files = ref<File[]>([]);
    const resetFiles = () => {
      files.value = [];
    };

    const fileFormat = computed((): FileFormat => {
      return detectFileFormat(fileRawText.value);
    });

    const contentOptionsFilter = computed((): string => {
      if (fileFormat.value == FileFormat.XML) return 'ContentOptions.Pain.00x';
      else if (fileFormat.value == FileFormat.SWIFT)
        return 'ContentOptions.Swift';
      else return 'ContentOptions';
    });

    const changeEditorLang = () => {
      if (contentEditor.value) {
        switch (fileFormat.value) {
          case FileFormat.XML:
            contentEditor.value?._editor.getSession().setMode('ace/mode/xml');
            break;
          default:
            contentEditor.value?._editor.getSession().setMode('ace/mode/text');
            break;
          //contentEditor.value?._editor.setOption<string>('lang','xml');
        }
      } else
        console.error(
          'Reference to editor not set, cant change language to ' +
            fileFormat.value.toString()
        );
    };

    const getUploadRequest = (): UploadRequest => {
      if (bankConnection.value?.ebicsVersion == 'H005') {
        return {
          orderService: btfType.value,
          signatureFlag: signatureFlag.value,
          edsFlag: requestEDS.value,
          fileName: fileName.value,
        } as UploadRequestH005;
      } else {
        //H004, H003
        return {
          orderType: orderType.value,
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
              getUploadRequest(),
              file
            )
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

    watch(fileFormat, changeEditorLang);

    return {
      bankConnection,
      activeBankConnections,
      hasActiveConnections,
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
      contentEditor,
      fileFormat,

      testInput,

      //Commons
      orderType,
      orderTypes,
      btfType,
      btfTypes,
      signatureFlag,
      requestEDS,
      signatureOZHNN,
      FileFormat,
      processUpload,
    };
  },
});
</script>
