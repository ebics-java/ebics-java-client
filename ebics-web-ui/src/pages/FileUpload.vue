<template>
  <q-page class="justify-evenly">
    <div class="q-pa-md">
      <h5>Upload File</h5>

      <div class="q-pa-md" style="max-width: 400px">
        <q-form @submit="onSubmit" @reset="onCancel" class="q-gutter-md">
          <q-select
            filled
            v-model="user"
            :options="users"
            :option-label="userLabel"
            label="EBICS User"
            hint="Select EBICS User"
            lazy-rules
            :rules="[(val) => val.id != 0 || 'Please select valid EBICS User']"
          />

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
            :option-label="t => btfLabel(t)"
            label="BTF Message Type"
            hint="Select EBICS BTF Message Type"
            lazy-rules
            :rules="[
              (val) =>
                val.id != 0 || 'Please select valid EBICS BTF Message Type',
            ]"
          />

          <!-- DZHNN / OZHNN -->
          <q-toggle v-if="user.ebicsVersion == 'H003' || user.ebicsVersion == 'H004'" v-model="signatureOZHNN" :label="signatureOZHNN ? 'Signature (OZHNN)' : 'No Signature (DZHNN)'"/>
          <!-- signature flag, request EDS -->
          
          <q-toggle v-if="user.ebicsVersion == 'H005'" v-model="signatureFlag" label="Signature flag"/>
          <q-toggle v-if="user.ebicsVersion == 'H005' && signatureFlag" v-model="requestEDS" label="Request EDS"/>
          

          <q-file
            style="max-width: 300px"
            v-model="file"
            outlined
            label="Max file size (20k)"
            max-file-size="20480"
            @rejected="onRejected"
            @update:model-value="onUpdateInputFile"
          >
            <template v-slot:prepend>
              <q-icon name="attach_file" />
            </template>
          </q-file>

          <q-input
            v-if="file && !binary"
            v-model="fileText"
            filled
            type="textarea"
            label="Input file content"
          />

          <div>
            <q-btn label="Upload" type="submit" color="primary" />
          </div>
        </q-form>
      </div>
    </div>
  </q-page>
</template>

<script lang="ts">
import { api } from 'boot/axios';
import { User, Btf, BtfMessage, UploadRequestH004 } from 'components/models';
import { defineComponent } from 'vue';
import { ref } from 'vue';
import useFileTransferAPI from 'components/filetransfer';

export default defineComponent({
  name: 'FileUpload',
  components: {},
  props: {},
  data() {
    return {
      file: ref<File | null>(null),
      fileText: 'text of the file',
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
        new Btf(
          'GLB',
          '',
          'CH',
          'zip',
          new BtfMessage('camt.054', '001', '03', 'XML')
        ),
        new Btf(
          'BTC',
          '',
          'CH',
          'xml',
          new BtfMessage('pain.001', '001', '09', 'XML')
        ),
        new Btf(
          'BTC',
          'URG',
          'DE',
          'xml',
          new BtfMessage('pain.001', '001', '09', 'XML')
        ),
      ],
      signatureFlag: true,
      requestEDS: true,
      signatureOZHNN: true,
    };
  },
  methods: {
    btfLabel(btf: Btf | undefined):string {
      return (btf instanceof Btf) ? btf.label() : ''
    },

    /**
     * Load text of the input file into text area
     * (only for text files, binary cant be edited)
     */
    onUpdateInputFile(file: File) {
      console.log(file.name);
      this.file = file;
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
    loadUsersData() {
      api
        .get<User[]>('/bankconnections')
        .then((response) => {
          this.users = response.data;
        })
        .catch((error: Error) => {
          this.$q.notify({
            color: 'negative',
            position: 'bottom-right',
            message: `Loading failed: ${error.message}`,
            icon: 'report_problem',
          });
        });
    },
    async onSubmit() { 
      console.log(this.file); 
      const uploadRequest = {
        orderType: this.orderType, 
        attributeType: this.signatureOZHNN ? 'OZHNN' : 'DZHNN',
        params: new Map(),
      } as UploadRequestH004;
      if (this.binary && this.file) {
        await this.ebicsUploadRequest(this.user, uploadRequest, this.file)
      } else if (!this.binary && this.fileText) {
        const content = new Blob([this.fileText], {type: 'text/html'});
        await this.ebicsUploadRequest(this.user, uploadRequest, content)
      } else {
        console.error('Invalid input combination, no file content available to upload')
      }
    },
    onCancel() {
      this.$router.go(-1);
    },
    onRejected() { //rejectedFiles: File[]
      this.$q.notify({
        type: 'negative',
        message:
          'File must smaller than 20kB, for bigger files use upload without editor',
      });
    },
  },
  mounted() {
    this.loadUsersData();
  },
  setup() {
    const {ebicsUploadRequest} = useFileTransferAPI();
    return {ebicsUploadRequest}
  }
});
</script>
