import { Duration } from '@core/model/duration.model';

export interface SplitItem extends Duration {
  numberOfRecords: number;
}
