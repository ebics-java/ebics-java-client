<template>
  <q-list>
    <div v-if="displaySection('General')">
      <q-item-label header>General Settings</q-item-label>
      <boolean-option
        label="Upload on drop"
        hint="Enable uploading of files after dropping for 'Simple file upload'"
        v-model="userSettings.uploadOnDrop"
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
        label="UETR for GPI"
        :hint="`unique UETR id based on random seed: ${this.uetr()}`"
        v-model="userSettings.adjustmentOptions.pain001.uetr"
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
    </div>
    <!--
    <q-separator spaced v-if="displaySection('ContentOptions.Pain.00x') && displaySection('ContentOptions.Swift')"/>
    -->
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
import { defineComponent } from 'vue';
import BooleanOption from 'src/components/BooleanOption.vue';
import useTextUtils from './text-utils';
import useUserSettingsAPI from './user-settings';
import { uuid } from 'vue-uuid';

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
    displaySection(sectionName: string): boolean {
      console.log(`sectionName: ${sectionName}, sectionFilter: ${this.sectionFilter}, includes: ${sectionName.includes(this.sectionFilter) ? '1': '0'}`)
      return (this.sectionFilter == '' || sectionName.includes(this.sectionFilter));
    },
    uetr(): string {
      return uuid.v4();
    },
  },
  setup() {
    const { userSettings } = useUserSettingsAPI();
    const { currentDate } = useTextUtils();
    return { currentDate, userSettings };
  },
});
</script>
