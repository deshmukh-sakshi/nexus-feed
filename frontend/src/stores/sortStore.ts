import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export type SortOption = 'new' | 'best' | 'hot'

interface SortState {
  sortOption: SortOption
  setSortOption: (option: SortOption) => void
}

export const useSortStore = create<SortState>()(
  persist(
    (set) => ({
      sortOption: 'new',
      setSortOption: (option) => set({ sortOption: option }),
    }),
    {
      name: 'sort-storage',
    }
  )
)
