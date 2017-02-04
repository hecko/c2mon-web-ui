import {Alarm} from './alarm';
import {IHttpService, IQService, IPromise, IDeferred} from 'angular';

export class AlarmService {
  public static $inject: string[] = ['$http', '$q'];

  public constructor(private $http: IHttpService, private $q: IQService) {}

  public getAlarm(id: number): IPromise<Alarm> {
    let q: IDeferred<Alarm> = this.$q.defer();

    this.$http.get('/api/alarms/' + id).then((response: any) => {
      q.resolve(response.data);
    });

    return q.promise;
  }

  public findAlarms(query: string): IPromise<Alarm[]> {
    let q: IDeferred<Alarm[]> = this.$q.defer();

    if (isNaN(Number(query))) {
      // If we have a non-numeric string, search by name
      this.$http.get('/api/alarms/search?query=' + '.*' + query + '.*').then((response: any) => {
        q.resolve(response.data);
      });

    } else {
      // Otherwise, look for an exact alarm by id
      this.$http.get('/api/alarms/' + query).then((response: any) => {
        q.resolve(response.data ? [response.data] : []);
      });
    }

    return q.promise;
  }

  public getActiveAlarms(): IPromise<Alarm[]> {
    let q: IDeferred<Alarm[]> = this.$q.defer();

    this.$http.get('/api/alarms/active').then((response: any) => {
      q.resolve(response.data);
    });

    return q.promise;
  }

  public getHistory(alarm: Alarm, min: number, max: number): IPromise<Alarm[]> {
    let q: IDeferred<Alarm[]> = this.$q.defer();

    this.$http.get('/api/alarms/' + alarm.id + '/history', {params: {min: min, max: max}}).then((response: any) => {
      q.resolve(response.data);
    });

    return q.promise;
  }
}
