import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import SkillBadge from '../SkillBadge.vue';

describe('SkillBadge Component', () => {
  it('renders badge label correctly', () => {
    const wrapper = mount(SkillBadge, {
      props: {
        label: 'Weather',
      },
    });

    expect(wrapper.text()).toContain('Weather');
  });

  it('renders count pill when count prop is provided', () => {
    const wrapper = mount(SkillBadge, {
      props: {
        label: 'NLP',
        count: 5,
      },
    });

    expect(wrapper.text()).toContain('NLP');
    expect(wrapper.text()).toContain('5');
  });

  it('emits click event when clicked', async () => {
    const wrapper = mount(SkillBadge, {
      props: {
        label: 'Finance',
      },
    });

    await wrapper.find('button').trigger('click');
    expect(wrapper.emitted('click')).toHaveLength(1);
  });

  it('applies active styling when active is true', () => {
    const wrapper = mount(SkillBadge, {
      props: {
        label: 'Active Filter',
        active: true,
      },
    });

    const button = wrapper.find('button');
    expect(button.classes()).toContain('bg-blue-600');
    expect(button.classes()).toContain('text-white');
  });
});
