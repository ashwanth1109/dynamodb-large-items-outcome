import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { ChartData } from '@core/model/chart-data.model';
import { CompressItem } from '@core/model/compress-item.model';
import { IterationItem } from '@core/model/iteration-item.model';
import { S3Item } from '@core/model/s3-item.model';
import { SplitItem } from '@core/model/split-item.model';
import { RestService } from '@core/services/rest.service';
import { BehaviorSubject, Observable } from 'rxjs';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnInit {
  private readonly staticColumns: string[] = ['no', 'size'];

  public isLoading = false;
  public displayedColumns: string[] = [
    ...this.staticColumns,
    'split',
    'compress',
    's3',
  ];
  public iterationList: IterationItem[] = [];

  public experiment = new FormGroup({
    split: new FormControl(true),
    compress: new FormControl(true),
    s3: new FormControl(true),
  });

  public form = new FormGroup({
    experiment: this.experiment,
    initialItemSize: new FormControl(400),
    incrementSize: new FormControl(400),
    iterator: new FormControl(1),
  });

  public chartData$ = new BehaviorSubject(null);
  public chartSeries$ = new BehaviorSubject(this.experiment.getRawValue());

  constructor(private readonly rest: RestService) {}

  public ngOnInit(): void {
    this.form.get('experiment').valueChanges.subscribe((values) => {
      this.displayedColumns = [...this.staticColumns];

      if (values.split) {
        this.displayedColumns.push('split');
      }

      if (values.compress) {
        this.displayedColumns.push('compress');
      }

      if (values.s3) {
        this.displayedColumns.push('s3');
      }

      this.chartSeries$.next(values);
    });
  }

  public async run(): Promise<void> {
    this.isLoading = true;
    this.iterationList = [];
    const {
      experiment,
      initialItemSize,
      incrementSize,
      iterator,
    } = this.form.getRawValue();
    const observables: Observable<any>[] = [];
    const newIterationList: IterationItem[] = [];
    const chartData: ChartData = {
      split: [],
      compress: [],
      s3: [],
    };

    for (let i = 0; i < iterator; i++) {
      const promises: Promise<SplitItem | CompressItem | S3Item>[] = [];

      promises.push(
        experiment.split
          ? this.rest
              .splitWrite(initialItemSize + incrementSize * i)
              .toPromise()
          : null,
        experiment.compress
          ? this.rest
              .compressWrite(initialItemSize + incrementSize * i)
              .toPromise()
          : null,
        experiment.s3
          ? this.rest.s3Write(initialItemSize + incrementSize * i).toPromise()
          : null
      );

      const [splitResult, compressResult, s3Result] = await Promise.all(
        promises
      );
      newIterationList[i] = {
        split: splitResult as SplitItem,
        compress: compressResult as CompressItem,
        s3: s3Result as S3Item,
      };
      chartData.split.push(splitResult?.duration);
      chartData.compress.push(compressResult?.duration);
      chartData.s3.push(s3Result?.duration);
      this.chartData$.next({ ...chartData });
      this.iterationList = [...newIterationList];
      await this.delay(1000);
    }

    this.isLoading = false;
  }

  public isTableShown(): boolean {
    const { split, compress, s3 } = this.experiment.getRawValue();
    return this.iterationList.length && (split || compress || s3);
  }

  private delay(ms: number): Promise<void> {
    return new Promise((resolve) => {
      // Adding a delay using setTimeout
      setTimeout(() => {
        resolve();
      }, ms);
    });
  }
}
