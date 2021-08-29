<template>
  <q-list>
    <div v-if="displaySection('General')">
      <q-item-label header>General Settings</q-item-label>
      <boolean-option
        label="Edit before upload"
        hint="Enable by default file editor for non-binary files"
        v-model="userSettings.fileEditor"
      />
      <boolean-option
        label="Tester settings"
        hint="Enable smart adjustments of uploaded files"
        v-model="userSettings.testerSettings"
      />
      <boolean-option
        :disable="!userSettings.testerSettings"
        label="Adjust file authomatically"
        hint="Apply smart adjustmets as bellow for every uploaded file authomatically, if disabled you can still apply adjustmets explicitelly"
        v-model="userSettings.adjustmentOptions.applyAuthomatically"
      />
      <q-separator spaced />
    </div>
    <div v-if="displaySection('ContentOptions.Pain.00x')">
      <q-item-label header>Smart adjustments for Pain.00x</q-item-label>
      <boolean-option
        :disable="!userSettings.testerSettings"
        label="msgId"
        hint="unique id based on current timestamp and user id"
        v-model="userSettings.adjustmentOptions.pain001.msgId"
      />
      <boolean-option
        label="pmtInfId"
        hint="unique id based on current timestamp, user id and B-Level"
        v-model="userSettings.adjustmentOptions.pain001.pmtInfId"
      />
      <boolean-option
        label="endToEndId"
        hint="unique id based on current timestamp, user id and B/C-Level"
        v-model="userSettings.adjustmentOptions.pain001.endToEndId"
      />
      <boolean-option
        label="instrId"
        hint="unique id based on current timestamp, user id and B/C-Level"
        v-model="userSettings.adjustmentOptions.pain001.instrId"
      />
      <boolean-option
        label="creDtTm"
        :hint="`actual date-time in ISO format: ${new Date().toISOString()}`"
        v-model="userSettings.adjustmentOptions.pain001.creDtTm"
      />
      <boolean-option
        label="reqdExctnDt"
        :hint="`actual date in YYYY-MM-DD format: ${this.currentDate()}`"
        v-model="userSettings.adjustmentOptions.pain001.reqdExctnDt"
      />
      <boolean-option
        label="nbOfTrxs"
        hint="recalculates number of transaction based on C-Levels"
        v-model="userSettings.adjustmentOptions.pain001.nbOfTrxsCalc"
      />
      <boolean-option
        label="ctrlSum"
        hint="recalculates control sum based on C-Level amouths"
        v-model="userSettings.adjustmentOptions.pain001.ctrlSumCalc"
      />
      <q-separator spaced />
    </div>
    <div
      v-if="
        userSettings.testerSettings && displaySection('ContentOptions.Swift')
      "
    >
      <q-item-label header>Smart adjustments for MT101</q-item-label>
      <boolean-option
        label=":20 (Message ID)"
        hint="unique id based on current timestamp and random"
        v-model="userSettings.adjustmentOptions.mt101.f20"
      />
      <boolean-option
        label=":21 (Transaction ID)"
        hint="unique id based on current timestamp and random"
        v-model="userSettings.adjustmentOptions.mt101.f21"
      />
    </div>
  </q-list>
</template>

<script lang="ts">
import { ref, defineComponent } from 'vue';
import BooleanOption from 'src/components/BooleanOption.vue';
import { UserSettings } from 'components/models';
import useTextUtils from './text-utils';

export default defineComponent({
  name: 'ContentAdjustmenOption',
  components: { BooleanOption },
  props: {
    sectionFilter: {
      type: String,
      requred: false,
      default: '',
    },
  },
  methods: {
    displaySection(sectionName: string) {
      return (
        this.sectionFilter == '' || this.sectionFilter.includes(sectionName)
      );
    },
  },
  setup() {
    const { currentDate } = useTextUtils();
    const userSettings = ref<UserSettings>({
      fileEditor: true,
      testerSettings: true,
      adjustmentOptions: {
        applyAuthomatically: true,
        pain001: {
          msgId: true,
          pmtInfId: false,
          instrId: true,
          endToEndId: true,
          uetr: true,
          nbOfTrxsCalc: true,
          ctrlSumCalc: true,
          reqdExctnDt: true,
          creDtTm: true,
          idPrefix: 't51246',
        },
        mt101: {
          uetr: true,
          f20: true,
          f21: false,
        },
      },
    });
    return { currentDate, userSettings };
  },
});
</script>
