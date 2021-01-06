import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class FilegenService {
  public getContentOfSize(sizeInKB: number): string {
    return '#'.repeat(sizeInKB * 1024);
  }
}
