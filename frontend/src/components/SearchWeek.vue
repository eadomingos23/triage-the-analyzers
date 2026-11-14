
<template>
  <el-form inline>
    <el-form-item label="Pick a week:">
      <div class="styled-week-input">
        <input
            type="week"
            v-model="selectedWeekNumber"
            @change="emitSearchCriteria"
            :min="firstWeek.weekNumber"
            :max="lastWeek.weekNumber"
            :disabled="!firstWeek.weekNumber || !lastWeek.weekNumber"
            class="el-input__inner"
            ref="weekInput"
            @click="showNativeWeekPicker"
        />
      </div>
    </el-form-item>

    <el-form-item>
      <div class="week-navigation">
        <el-button
            @click="goToPreviousWeek"
            :disabled="isFirstWeek"
            type="primary"
            plain
            :icon="ArrowLeft"
        >
          Previous Week
        </el-button>

        <span class="week-range">{{ selectedWeekDateRange }}</span>

        <el-button
            @click="goToNextWeek"
            :disabled="isLastWeek"
            type="primary"
            plain
            :icon="ArrowRight"
        >
          Next Week
        </el-button>
      </div>
    </el-form-item>

    <el-form-item>
      <el-button @click="emitCurrentWeek" type="primary" plain>
        Go to current week
      </el-button>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useSettingsStore } from '@/stores/settings'
import { findSectionById } from '@/apis/section'
import type { FindSectionByIdResponse, WeekInfo } from '@/apis/section/types'
import { getAllWeeksInSection, getCurrentWeekNumber } from '@/utils/week'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'

/*
selectedWeekNumber will hold the week number of the selected week, e.g., 2025-W25.
By default, it will be the current week number.
The initial value is assigned by the parent component.
*/
const selectedWeekNumber = defineModel<string>()

const allWeeks = ref<WeekInfo[]>([])

const selectedWeekDateRange = computed(() => {
  const selected = allWeeks.value.find(
      (week) => week.weekNumber === selectedWeekNumber.value
  )

  return selected
      ? `${selected.monday} -- ${selected.sunday}`
      : 'The picked week is not in the current section.'
})

const firstWeek = ref<WeekInfo>({
  weekNumber: '',
  monday: '',
  sunday: '',
  isActive: false
})

const lastWeek = ref<WeekInfo>({
  weekNumber: '',
  monday: '',
  sunday: '',
  isActive: false
})

const settingsStore = useSettingsStore()

const weekInput = ref<HTMLInputElement | null>(null)

onMounted(async () => {
  if (!settingsStore.defaultSectionId) {
    return
  }

  const result: FindSectionByIdResponse = await findSectionById(
      settingsStore.defaultSectionId
  )

  const currentSection = result.data

  if (!currentSection) {
    return
  }

  // Get all weeks in the current section.
  allWeeks.value = getAllWeeksInSection(currentSection)

  // Safely check the first and last elements before assigning them.
  const first = allWeeks.value[0]
  const last = allWeeks.value[allWeeks.value.length - 1]

  if (first !== undefined && last !== undefined) {
    firstWeek.value = first
    lastWeek.value = last
  }
})

/*
Show the native week picker dialog.
*/
const showNativeWeekPicker = () => {
  if (weekInput.value && typeof weekInput.value.showPicker === 'function') {
    weekInput.value.showPicker()
  }
}

// Emits
const emit = defineEmits(['search', 'currentWeek'])

// Methods
const emitSearchCriteria = () => {
  const selectedWeekObj = allWeeks.value.find(
      (week) => week.weekNumber === selectedWeekNumber.value
  )

  // If the selected week is not in the current section, reset to current week.
  if (!selectedWeekObj) {
    ElMessage.error('Selected week is outside the current section. Resetting to current week.')
    selectedWeekNumber.value = getCurrentWeekNumber()
  }

  emit('search')
}

const emitCurrentWeek = () => {
  selectedWeekNumber.value = getCurrentWeekNumber()
  emit('currentWeek')
}

// Utility to find index of selected week.
const findWeekIndex = () => {
  return allWeeks.value.findIndex(
      (week) => week.weekNumber === selectedWeekNumber.value
  )
}

// Safely navigate to the previous week.
const goToPreviousWeek = () => {
  const index = findWeekIndex()

  if (index > 0) {
    const previousWeek = allWeeks.value[index - 1]

    if (previousWeek !== undefined) {
      selectedWeekNumber.value = previousWeek.weekNumber
      emit('search')
    }
  }
}

// Safely navigate to the next week.
const goToNextWeek = () => {
  const index = findWeekIndex()

  if (index >= 0 && index < allWeeks.value.length - 1) {
    const nextWeek = allWeeks.value[index + 1]

    if (nextWeek !== undefined) {
      selectedWeekNumber.value = nextWeek.weekNumber
      emit('search')
    }
  }
}

const isFirstWeek = computed(
    () => selectedWeekNumber.value === firstWeek.value.weekNumber
)

const isLastWeek = computed(
    () => selectedWeekNumber.value === lastWeek.value.weekNumber
)
</script>

<style scoped lang="scss">
.week-navigation {
  display: flex;
  align-items: center;

  .el-button {
    white-space: nowrap;
  }

  .week-range {
    flex-shrink: 0;
    min-width: 190px;
    text-align: center;
    font-weight: bold;
  }
}

@media (max-width: 600px) {
  .week-navigation {
    flex-direction: column;
    align-items: stretch;

    .week-range {
      margin: 8px 0;
      text-align: center;
    }
  }
}

.styled-week-input {
  display: inline-block;
  width: 160px;
  position: relative;
  font-size: 14px;
  line-height: 30px;
}

input[type='week'].el-input__inner {
  -webkit-appearance: none;
  appearance: none;
  width: 100%;
  padding: 0 11px;
  height: 30px;
  line-height: 30px;
  font-size: 14px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-sizing: border-box;
  transition: border-color 0.2s cubic-bezier(0.645, 0.045, 0.355, 1);
  outline: none;

  &:focus {
    border-color: #409eff;
  }
}
</style>
