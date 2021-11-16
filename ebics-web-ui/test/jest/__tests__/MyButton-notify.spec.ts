import { describe, expect, it } from '@jest/globals';
import { installQuasarPlugin } from '@quasar/quasar-app-extension-testing-unit-jest';
import { mount } from '@vue/test-utils';
import { Notify } from 'quasar';
import MyButton from './demo1/MyButton';

//Notify plugin is used by notify function of MyButton
installQuasarPlugin({ plugins: { Notify } });

describe('MyButton', () => {
  it('test quasar notify plugin call withing component function doesn crash', () => {
    const wrapper = mount(MyButton);
    const { vm } = wrapper;

    expect(typeof vm.notify).toBe('function');
    
    vm.notify();
    
    expect(true).toBeTruthy();
  });

});
