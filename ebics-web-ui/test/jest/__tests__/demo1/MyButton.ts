import { useQuasar } from 'quasar';
import { defineComponent, ref } from 'vue';

export default defineComponent({
  name: 'MyButton',
  props: {
    incrementStep: {
      type: Number,
      default: 1,
    },
  },
  setup(props) {
    const q = useQuasar();
    const counter = ref(0);
    const input = ref('rocket muffin');
    const increment = () => {
      counter.value += props.incrementStep;
    };

    const notify = () => {
      q.notify({
        color: 'positive',
        position: 'bottom-right',
        message: 'test',
        icon: 'gpp_good',
      });
    }

    return { counter, input, notify, increment };
  },
});
