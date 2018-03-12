import sys
import logging
import time


_logging_initialized = False

def getLogger(name):
    global _logging_initialized
    if not _logging_initialized:
        logging.basicConfig(
            level=logging.INFO,
            format='%(levelname)s: %(message)s'
            #format='%(levelname)s: %(name)s: %(message)s'
        )
        logging.getLogger('requests').setLevel(logging.WARN)
        _logging_initialized = True
    return logging.getLogger(name)


def warn(msg, *args):
    print >> sys.stderr, "warning:", unicode(msg) % args


def info(msg, *args):
    print >> sys.stderr, unicode(msg) % args


def as_dataframe(iterable, levels=None, columns=None, transform=None):
    """Convert nested iterables into a DataFrame with multi-level index."""
    import pandas as pd
    if not transform:
        transform = lambda x: x
    if not levels or len(levels) == 1:
        return pd.DataFrame(
            (transform(row) for row in iterable),
            columns=columns
        )
        # TODO: if len(levels) == 1, set index name?
    else:
        return pd.concat(
            (as_dataframe(
                sub, levels=levels[1:], columns=columns, transform=transform
            ) for sub in iterable),
            keys=xrange(len(iterable)),
            names=levels
        )


class Results(list):
    """Wrapper that enables a list of results to be converted to a DataFrame."""

    def __init__(self, iterable, levels=None, columns=None, transform=None):
        super(Results, self).__init__(iterable)
        self.levels = levels
        self.columns = columns
        self.transform = transform

    def as_dataframe(self):
        return as_dataframe(
            self, levels=self.levels, columns=self.columns,
            transform=self.transform
        )

    @classmethod
    def decorate(cls, levels=None, columns=None, transform=None):
        """Decorate a function to return a Results wrapper."""
        def decorator(func):
            def newfunc(*args, **kwargs):
                return cls(
                    func(*args, **kwargs),
                    levels=levels, columns=columns, transform=transform
                )
            newfunc.__name__ = func.__name__
            newfunc.__doc__ = func.__doc__
            return newfunc
        return decorator


def dhms(seconds):
    seconds = int(seconds + 0.5)
    days = seconds // 86400; seconds %= 86400
    hours = seconds // 3600; seconds %= 3600
    minutes = seconds // 60; seconds %= 60
    return (days, hours, minutes, seconds)


class Timer(object):

    def __init__(self, steps_total):
        self.steps_total = steps_total
        self.start_time = None
        self.stop_time = None
        self._logger = getLogger(self.__class__.__name__)

    def start(self):
        self.start_time = time.time()
        return self

    def update(self, steps_complete):
        now = time.time()
        percent_complete = float(steps_complete) / float(self.steps_total) * 100.0
        elapsed = now - self.start_time
        rate = steps_complete / elapsed
        steps_remaining = self.steps_total - steps_complete
        time_remaining = steps_remaining / rate
        days, hours, minutes, seconds = dhms(time_remaining)
        self._logger.info(
            "%d/%d (%.3g%%) complete; ~%dd%02dh%02dm%02ds remaining",
            steps_complete, self.steps_total,
            percent_complete,
            days, hours, minutes, seconds)

    def stop(self):
        if not self.stop_time:
            self.stop_time = time.time()
        elapsed = self.stop_time - self.start_time
        days, hours, minutes, seconds = dhms(elapsed)
        self._logger.info(
            "complete; elapsed time = %dd%02dh%02dm%02ds",
            days, hours, minutes, seconds)


if __name__ == '__main__':
    import doctest
    print doctest.testmod()
