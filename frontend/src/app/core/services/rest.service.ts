import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { CompressItem } from '@core/model/compress-item.model';
import { S3Item } from '@core/model/s3-item.model';
import { SplitItem } from '@core/model/split-item.model';
import { Observable } from 'rxjs';

import config from '../../../../aws-exports.json';

@Injectable({
  providedIn: 'root',
})
export class RestService {
  private readonly apiUrl = config.aws_rest_api_url;

  constructor(private readonly http: HttpClient) {}

  public splitWrite(itemSize: number): Observable<SplitItem> {
    return this.http.post<SplitItem>(`${this.apiUrl}split-write`, itemSize);
  }

  public s3Write(itemSize: number): Observable<S3Item> {
    return this.http.post<S3Item>(`${this.apiUrl}s3-write`, itemSize);
  }

  public compressWrite(itemSize: number): Observable<CompressItem> {
    return this.http.post<CompressItem>(`${this.apiUrl}compress-write`, itemSize);
  }
}
