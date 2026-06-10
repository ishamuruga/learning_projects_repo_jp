export interface Todo {
  id: number;
  text: string;
  priority: 'low' | 'medium' | 'high';
  plannedEndDate?: string;
  completed: boolean;
  createdAt: Date;
}

export type FilterType = 'all' | 'active' | 'completed';
