import {
  AfterViewInit,
  Component,
  Input,
  OnChanges,
  SimpleChanges,
} from '@angular/core';
import { ChartData } from '@core/model/chart-data.model';
import { ChartSeriesStatus } from '@core/model/chart-series-status.model';
import { KeyMap } from '@core/model/key-map.model';
import * as Highcharts from 'highcharts';
import { Chart, Series } from 'highcharts';
import theme from 'highcharts/themes/dark-unica';

theme(Highcharts);

@Component({
  selector: 'app-chart',
  templateUrl: './chart.component.html',
  styleUrls: ['./chart.component.scss'],
})
export class ChartComponent implements OnChanges, AfterViewInit {
  private seriesDefaultMap = {
    split: {
      id: 'split',
      name: 'Split',
      data: [],
      type: 'line',
    },
    compress: {
      id: 'compress',
      name: 'Compress',
      data: [],
      type: 'line',
    },
    s3: {
      id: 's3',
      name: 'S3',
      data: [],
      type: 'line',
    },
  };
  private seriesMap: KeyMap<Series> = {};

  @Input()
  public data: ChartData;

  @Input()
  public seriesStatus: ChartSeriesStatus;

  public highcharts: typeof Highcharts = Highcharts;
  public chartOptions: Highcharts.Options = {
    title: {
      text: 'Experiments trend',
    },
    subtitle: {
      text: 'Lower is better',
    },
    yAxis: {
      title: {
        text: 'Milliseconds',
      },
    },
    xAxis: {
      tickInterval: 1,
    },
    series: [],
  };
  public updateFlag = false;

  public ngAfterViewInit(): void {
    this.seriesToMap();
  }

  public ngOnChanges(changes: SimpleChanges): void {
    console.log(changes);
    if (
      changes.data?.currentValue &&
      changes.data?.currentValue !== changes.data?.previousValue
    ) {
      this.updateData(changes.data.currentValue);
    }

    if (
      changes.seriesStatus?.currentValue &&
      changes.seriesStatus?.currentValue !== changes.seriesStatus?.previousValue
    ) {
      this.updateSeriesStatus();
    }
  }

  public updateSeriesStatus(): void {
    const chart = this.getChart();

    if (!chart) {
      return;
    }

    Object.keys(this.seriesStatus).forEach((key) => {
      if (!this.seriesStatus[key]) {
        this.seriesMap[key]?.remove();
        delete this.seriesMap[key];
      } else if (!this.seriesMap[key]) {
        const newSeries = { ...this.seriesDefaultMap[key] };
        newSeries.data = this.getSeriesData(this.data[key]);
        this.seriesMap[key] = chart.addSeries(newSeries);
      }
    });
  }

  public updateData(data: ChartData): void {
    const series = [];

    Object.keys(this.seriesStatus).forEach((key) => {
      if (this.seriesStatus[key]) {
        series.push({
          id: key,
          name: this.capitalize(key),
          data: this.getSeriesData(data[key]),
          type: 'line',
        });
      }
    });

    this.chartOptions.series = series;
    this.seriesToMap();
    this.updateFlag = true;
  }

  private seriesToMap(): void {
    const chart = this.getChart();

    if (!chart) {
      return;
    }

    this.seriesMap = {};

    chart.series.forEach((item) => {
      this.seriesMap[item.options.id] = item;
    });
  }

  private getChart(): Chart {
    return this.highcharts.charts[this.highcharts.charts.length - 1];
  }

  private getSeriesData(data?: number[]): number[] {
    return (data || []).filter((value) => !!value);
  }

  private capitalize(str: string): string {
    return `${str.charAt(0).toUpperCase()}${str.slice(1)}`;
  }
}
