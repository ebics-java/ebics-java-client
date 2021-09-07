<template>
  <q-page class="justify-evenly" ref="testInput">
    <div v-if="hasActiveConnections" class="q-pa-md">
      <h5>Simple file download</h5>

      <!-- style="max-width: 400px" -->
      <div class="q-pa-md">
        <q-form ref="uploadForm" @submit="processDownload" class="q-gutter-md">
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
          <div class="q-pa-md q-gutter-sm">
            <q-btn label="Download" type="submit" color="primary" />
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
        connection in order to download files.
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
  DownloadRequest,
  DownloadRequestH004,
  DownloadRequestH005,
  FileFormat,
  BTFType,
  OrderTypeFilter,
  OrderType,
} from 'components/models';
import { defineComponent } from 'vue';
import { ref, computed } from 'vue';
import { exportFile } from 'quasar';

//Composition APIs
import useBankConnectionsAPI from 'components/bankconnections';
import useFileTransferAPI from 'components/filetransfer';
import useTextUtils from 'components/text-utils';
import useUserSettings from 'components/user-settings';
import useOrderTypesAPI from 'components/order-types';

export default defineComponent({
  name: 'FileDownload',
  setup() {
    //Selected bank connection
    const bankConnection = ref<User>();
    const replaceMsgId = ref(true);
    const { activeBankConnections, hasActiveConnections, bankConnectionLabel } =
      useBankConnectionsAPI();
    const { ebicsDownloadRequest } = useFileTransferAPI();
    const { applySmartAdjustments, detectFileFormat, getFileExtension } = useTextUtils();
    const { userSettings } = useUserSettings();
    const { btfTypes, orderTypes, orderTypeLabel, btfTypeLabel } =
      useOrderTypesAPI(bankConnection, ref(OrderTypeFilter.DownloadOnly));

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

    const getDownloadRequest = (): DownloadRequest => {
      if (bankConnection.value?.ebicsVersion == 'H005') {
        return {
          orderService: btfType.value?.service,
        } as DownloadRequestH005;
      } else {
        //H004, H003
        return {
          orderType: orderType.value?.orderType,
          params: new Map(),
        } as DownloadRequestH004;
      }
    };

    const processDownload = async (): Promise<void> => {
      if (bankConnection.value) {
        const fileData = await ebicsDownloadRequest(bankConnection.value, getDownloadRequest());
        
        if (fileData) {
          const fileFormat = detectFileFormat(fileData);
          let fileExtension = getFileExtension(fileFormat);
          const status = exportFile('download.' + fileExtension, fileData, {mimeType: 'application/xml'});
          if (status !== true) {
            console.error(`Browser error by downloading of the file: ${JSON.stringify(status)}`)
          }
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
      processDownload,
    };
  },
});
</script>
