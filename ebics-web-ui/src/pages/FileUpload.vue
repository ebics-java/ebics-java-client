<template>
  <q-page class="justify-evenly">
    <div v-if="hasActiveConnections" class="q-pa-md">
      <h5 v-if="fileEditor">Edit &amp; upload File</h5>
      <h5 v-else>Simple file upload</h5>

      <!-- style="max-width: 400px" -->
      <div class="q-pa-md">
        <q-form @submit="onSubmit" @reset="onCancel" class="q-gutter-md">
          <q-select
            filled
            v-model="user"
            :options="activeBankConnections"
            :option-label="userLabel"
            label="EBICS Bank connection"
            hint="Select EBICS bank connection"
            lazy-rules
            :rules="[(val) => val.id != 0 || 'Please select valid EBICS User']"
          />

          <div class="q-gutter-sm">
            <q-radio
              v-model="user.ebicsVersion"
              val="H003"
              label="EBICS 2.4 (H003)"
            />
            <q-radio
              v-model="user.ebicsVersion"
              val="H004"
              label="EBICS 2.5 (H004)"
            />
            <q-radio
              v-model="user.ebicsVersion"
              val="H005"
              label="EBICS 3.0 (H005)"
            />
          </div>

          <q-select
            v-if="user.ebicsVersion == 'H003' || user.ebicsVersion == 'H004'"
            filled
            v-model="orderType"
            :options="orderTypes"
            label="EBICS Order Type"
            hint="Select EBICS Order Type"
            lazy-rules
            :rules="[
              (val) => val.id != 0 || 'Please select valid EBICS Order Type',
            ]"
          />

          <q-select
            v-if="user.ebicsVersion == 'H005'"
            filled
            v-model="btfType"
            :options="btfTypes"
            :option-label="(t) => btfLabel(t)"
            label="BTF Message Type"
            hint="Select EBICS BTF Message Type"
            lazy-rules
            :rules="[
              (val) =>
                val.id != 0 || 'Please select valid EBICS BTF Message Type',
            ]"
          />

          <!-- DZHNN / OZHNN -->
          <q-toggle
            v-if="user.ebicsVersion == 'H003' || user.ebicsVersion == 'H004'"
            v-model="signatureOZHNN"
            :label="
              signatureOZHNN ? 'Signature (OZHNN)' : 'No Signature (DZHNN)'
            "
          />
          <!-- signature flag, request EDS -->

          <q-toggle
            v-if="user.ebicsVersion == 'H005'"
            v-model="signatureFlag"
            label="Signature flag"
          />
          <q-toggle
            v-if="user.ebicsVersion == 'H005' && signatureFlag"
            v-model="requestEDS"
            label="Request EDS"
          />

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
            v-model="file"
            outlined
            multiple
            label="Max file size (1GB)"
            max-file-size="1200000000"
            @rejected="onRejected"
            @update:model-value="onUpdateInputFile"
          >
            <template v-slot:prepend>
              <q-icon name="attach_file" />
            </template>
          </q-file>

          <!-- q-input
            v-if="file && !binary"
            v-model="fileText"
            filled
            type="textarea"
            label="Input file content"
          /-->

          <v-ace-editor
            ref="contentEditor"
            v-if="fileEditor"
            v-model:value="fileText"
            lang="xml"
            theme="clouds"
            style="height: 300px"
            :printMargin="false"
            @init="initEditor"
          />

          <q-input
            v-if="user.ebicsVersion == 'H005'"
            filled
            v-model="fileName"
            label="Uploaded filename"
            hint="For support purposes only"
          />

          <div class="q-pa-md q-gutter-sm">
            <q-btn-dropdown
              split
              color="primary"
              label="Smart Adjust"
              @click="setUniqueIds()"
            >
              <user-preferences section-filter="ContentOptions.Pain.00x" />
            </q-btn-dropdown>

            <q-btn label="Upload" type="submit" color="primary" />
          </div>
        </q-form>
      </div>
    </div>
    <div v-else class="q-pa-md">
      <q-banner class="bg-grey-3">
        <template v-slot:avatar>
          <q-icon name="signal_wifi_off" color="primary" />
        </template>
        You have no initialized bank connection. Create and initialize one bank connection in order to upload files.
        <template v-slot:action>
          <q-btn flat color="primary" label="Manage bank connections" to="/bankconnections" />
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
  AutoAdjustmentsPain00x,
} from 'components/models';
import { defineComponent } from 'vue';
import { ref } from 'vue';

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
    return {
      file: ref<File | null>(null),
      fileText: ref<string>('<document>paste document here</document>'),
      fileName: '',
      binary: false,
      users: [] as User[],
      user: {
        userId: '',
        name: '',
      } as User,
      orderType: '',
      orderTypes: ['XE2', 'XE3', 'XL3', 'XG1', 'CCT'],
      btfType: undefined as Btf | undefined,
      btfTypes: [
        new Btf('PSR', undefined, 'CH', 'ZIP', new BtfMessage('pain.002')),
        new Btf('MCT', undefined, 'CH', undefined, new BtfMessage('pain.001')),
        new Btf('MCT', 'XCH', 'CGI', undefined, new BtfMessage('pain.001')),
      ],
      signatureFlag: true,
      requestEDS: true,
      signatureOZHNN: true,
    };
  },
  methods: {
    initEditor(editor: VAceEditorInstance) {
      console.log(`Initialize ace editor: ${JSON.stringify(editor.$options)}`);
    },
    async setUniqueIds() {
      this.fileText = await this.applySmartAdjustmentsPain00x(
        this.fileText,
        this.userSettings?.adjustmentOptions.pain001 as AutoAdjustmentsPain00x
      );
    },

    btfLabel(btf: Btf | undefined): string {
      return btf instanceof Btf ? btf.label() : '';
    },

    /**
     * Load text of the input file into text area
     * (only for text files, binary cant be edited)
     */
    onUpdateInputFile(file: File) {
      console.log(file.name);
      this.file = file;
      this.fileName = file.name;
      file
        .text()
        .then((text) => {
          //Detect binary - open binary data in a normal way (without using a hex editor),
          //it will encounter some rendering problems which translate to you as a succession
          //of this weird character � called "Replacement character" == ufffd
          if (/\ufffd/.test(text) === true) {
            this.binary = true;
            this.$q.notify({
              color: 'positive',
              position: 'bottom-right',
              message:
                "Binary files can't be edited, although you can still upload the file",
              icon: 'warning',
            });
          } else {
            this.binary = false;
            this.fileText = text;
          }
        })
        .catch((error: Error) => {
          this.$q.notify({
            color: 'negative',
            position: 'bottom-right',
            message: `Loading file failed: ${error.message}`,
            icon: 'report_problem',
          });
        });
    },

    /**
     * Display label of the user
     */
    userLabel(user: User | null): string {
      if (
        user !== null &&
        user.userId.trim().length > 0 &&
        user.name.trim().length > 0
      ) {
        return `${user.userId} | ${user.name}`;
      } else {
        return '';
      }
    },
    async onSubmit() {
      console.log(this.file);
      var uploadRequest: UploadRequest;
      if (this.user.ebicsVersion == 'H005') {
        uploadRequest = {
          orderService: this.btfType,
          signatureFlag: this.signatureFlag,
          edsFlag: this.requestEDS,
          fileName: this.file ? this.file.name : 'Filename_not_provided',
        } as UploadRequestH005;
      } else {
        //H004, H003
        uploadRequest = {
          orderType: this.orderType,
          attributeType: this.signatureOZHNN ? 'OZHNN' : 'DZHNN',
          params: new Map(),
        } as UploadRequestH004;
      }
      if (this.binary && this.file) {
        await this.ebicsUploadRequest(this.user, uploadRequest, this.file);
      } else if (!this.binary && this.fileText) {
        const content = new Blob([this.fileText], { type: 'text/html' });
        await this.ebicsUploadRequest(this.user, uploadRequest, content);
      } else {
        console.error(
          'Invalid input combination, no file content available to upload'
        );
      }
    },
    onCancel() {
      this.$router.go(-1);
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
  setup() {
    const replaceMsgId = ref(true);
    const { activeBankConnections, hasActiveConnections } =
      useBankConnectionsAPI();
    const { ebicsUploadRequest } = useFileTransferAPI();
    const { applySmartAdjustmentsPain00x } = useTextUtils();
    const { userSettings } = useUserSettings();
    return {
      activeBankConnections,
      hasActiveConnections,
      userSettings,
      replaceMsgId,
      ebicsUploadRequest,
      applySmartAdjustmentsPain00x,
    };
  },
});
</script>
