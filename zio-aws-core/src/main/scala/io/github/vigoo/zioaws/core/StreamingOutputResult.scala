package io.github.vigoo.zioaws.core

import zio.stream.ZStream

case class StreamingOutputResult[R, Response, Item](
    response: Response,
    output: ZStream[R, AwsError, Item]
) {
  def mapResponse[Response2](
      f: Response => Response2
  ): StreamingOutputResult[R, Response2, Item] =
    copy(response = f(response))

  def mapOutput[I](
      f: ZStream[R, AwsError, Item] => ZStream[R, AwsError, I]
  ): StreamingOutputResult[R, Response, I] =
    copy(output = f(output))

  def provide(r: R): StreamingOutputResult[Any, Response, Item] =
    StreamingOutputResult(response, output.provide(r))
}
