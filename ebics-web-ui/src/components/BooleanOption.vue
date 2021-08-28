<template>
  <q-item tag="label" v-ripple>
    <q-item-section>
      <q-item-label>{{ label }}</q-item-label>
      <q-item-label caption>{{ hint }}</q-item-label>
    </q-item-section>
    <q-item-section side>
      <q-toggle v-model="value" />
    </q-item-section>
  </q-item>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
  name: 'BooleanOption',
  props: {
    label: {
      type: String,
      required: true,
    },
    hint: {
      type: String,
      required: true,
    },
    modelValue: {
      type: Boolean,
      required: true,
    },
  },
  emits: ['update:modelValue'],
  computed: {
    value: {
      get(): boolean {
        return this.modelValue;
      },
      set(value: boolean) {
        this.$emit('update:modelValue', value);
      },
    },
  },
});
</script>
