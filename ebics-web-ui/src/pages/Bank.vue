<template>
  <q-page class="justify-evenly">
    <div class="q-pa-md">
      <div class="q-pa-md" style="max-width: 400px">
        <q-form @submit="onSubmitBank" @reset="onCancel" class="q-gutter-md">
          <q-input
            filled
            v-model="bank.name"
            label="Bank name *"
            hint="User defined name for this bank"
            lazy-rules
            :rules="[
              (val) =>
                (val && val.length > 1) ||
                'Bank name must be at least 2 characters',
            ]"
          />

          <q-input
            filled
            v-model="bank.bankURL"
            label="EBICS URL"
            hint="EBICS bank URL, including https://"
            lazy-rules
            :rules="[
              (val) =>
                (val && val.length > 1 && validateUrl(val)) ||
                'Please enter valid URL including http(s)://',
            ]"
          />

          <q-input
            filled
            v-model="bank.hostId"
            label="EBICS HOSTID"
            hint="EBICS HOST ID, example EBXUBSCH"
            lazy-rules
            :rules="[
              (val) =>
                (val && val.length > 0) ||
                'Please enter valid EBICS HOST ID, at least 1 character',
            ]"
          />

          <q-field
            outlined
            label="Supported EBICS versions"
            stack-label
            hint="Not supported versions are shown and disabled"
          >
            <template v-slot:control>
              <q-checkbox
                v-for="vs of versionSettings"
                v-bind:key="vs.version"
                :label="vs.version"
                :disable="!vs.isSupportedByBank || !vs.isSupportedByClient"
                v-model="vs.isAllowedForUse"
              />

              <q-btn
                @click="loadVersionsSettings"
                label="Refresh"
                color="primary"
                flat
                class="q-ml-sm"
                icon="sync"
              />
            </template>
          </q-field>

          <div>
            <q-btn
              v-if="id === undefined"
              label="Add"
              type="submit"
              color="primary"
            />
            <q-btn v-else label="Update" type="submit" color="primary" />
            <q-btn
              label="Cancel"
              type="reset"
              color="primary"
              flat
              class="q-ml-sm"
              icon="undo"
            />
          </div>
        </q-form>
      </div>
    </div>
  </q-page>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import useBankAPI from 'components/bank';
import { useRouter } from 'vue-router';

export default defineComponent({
  name: 'Bank',
  props: {
    id: {
      type: Number,
      required: false,
      default: undefined,
    },
  },
  methods: {},
  setup(props) {
    const router = useRouter();
    const { bank, versionSettings, saveVersionsSettings, createOrUpdateBank, loadVersionsSettings, allowedVersionsCount } =
      useBankAPI(props.id);
    const onCancel = (): void => {
      router.go(-1);
    };
    const onSubmitBank = async (): Promise<void> => {
      await saveVersionsSettings();
      await createOrUpdateBank();
    };
    const validateUrl = (url: string): boolean => {
      const regex =
        /^(http(s)?:\/\/.)(www\.)?[-a-zA-Z0-9@:%._\+~#=]{0,256}\.[a-z]{2,6}\b([-a-zA-Z0-9@:%_\+.~#?&//=]*)$/;
      return regex.test(url);
    };
    return {
      bank,
      versionSettings,
      onCancel,
      onSubmitBank,
      validateUrl,
      loadVersionsSettings,
      saveVersionsSettings,
      createOrUpdateBank,
      allowedVersionsCount,
    };
  },
});
</script>
