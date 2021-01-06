import { CompressItem } from '@core/model/compress-item.model';
import { S3Item } from '@core/model/s3-item.model';
import { SplitItem } from '@core/model/split-item.model';

export interface IterationItem {
  split?: SplitItem;
  compress?: CompressItem;
  s3?: S3Item;
}
